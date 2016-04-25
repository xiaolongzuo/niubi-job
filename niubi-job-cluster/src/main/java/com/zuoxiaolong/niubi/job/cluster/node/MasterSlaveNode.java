/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zuoxiaolong.niubi.job.cluster.node;

import com.zuoxiaolong.niubi.job.api.MasterSlaveApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.MasterSlaveApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.api.data.MasterSlaveNodeData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.cluster.startup.Bootstrap;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.*;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主从模式的集群实现.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class MasterSlaveNode extends AbstractClusterJobNode {

    private CuratorFramework client;

    private final LeaderSelector leaderSelector;

    private InterProcessLock initLock;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    private MasterSlaveApiFactory masterSlaveApiFactory;

    private String nodePath;

    private PathChildrenCache jobCache;

    private PathChildrenCache nodeCache;

    public MasterSlaveNode() {
        LoggerHelper.info("Starting init master-slave node...");
        String zookeeperAddresses = Bootstrap.getZookeeperAddresses();
        LoggerHelper.info("Connect to Zk client [" + zookeeperAddresses + "]");
        this.client = CuratorFrameworkFactory.newClient(zookeeperAddresses, retryPolicy);
        LoggerHelper.info("Starting Zk client connection...");
        this.client.start();
        LoggerHelper.info("Zk client has been started...");
        this.masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);
        this.initLock = new InterProcessMutex(client, masterSlaveApiFactory.pathApi().getInitLockPath());
        try {
            this.initLock.acquire();
            LoggerHelper.info("get init lock... begin init jobs.");
            initJobs();
            LoggerHelper.info("init jobs successfully.");
        } catch (Exception e) {
            throw new NiubiException(e);
        } finally {
            try {
                this.initLock.release();
            } catch (Exception e) {
                throw new NiubiException(e);
            }
        }

        this.nodePath = masterSlaveApiFactory.nodeApi().saveNode(new MasterSlaveNodeData.Data(getNodeIp()));
        LoggerHelper.info("Zk Node has been created successfully...");
        this.nodeCache = new PathChildrenCache(client, PathHelper.getParentPath(masterSlaveApiFactory.pathApi().getNodePath()), true);
        this.nodeCache.getListenable().addListener(new NodeCacheListener());

        this.jobCache = new PathChildrenCache(client, masterSlaveApiFactory.pathApi().getJobPath(), true);
        this.jobCache.getListenable().addListener(new JobCacheListener());

        this.leaderSelector = new LeaderSelector(client, masterSlaveApiFactory.pathApi().getSelectorPath(), new MasterSlaveLeadershipSelectorListener());
        this.leaderSelector.autoRequeue();
        LoggerHelper.info("Init master-slave node successfully...");
    }

    /**
     * 如果没有任何一个节点存活的话,就改变所有的任务状态为shutdown.
     */
    private void initJobs() {
        List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
        if (!ListHelper.isEmpty(masterSlaveNodeDataList)) {
            return;
        }
        List<MasterSlaveJobData> masterSlaveJobDataList = new ArrayList<>();
        try {
            masterSlaveJobDataList = masterSlaveApiFactory.jobApi().getAllJobs();
        } catch (Throwable e) {
            if (e instanceof NiubiException) {
                e = e.getCause();
            }
            if (e instanceof KeeperException.NoNodeException) {
                LoggerHelper.info("job path not found. skip init jobs.");
            } else {
                LoggerHelper.warn("get jobs failed. ", e);
            }
        }
        for (MasterSlaveJobData masterSlaveJobData : masterSlaveJobDataList) {
            MasterSlaveJobData.Data data = masterSlaveJobData.getData();
            data.init();
            masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    @Override
    protected synchronized void doJoin() {
        leaderSelector.start();
        try {
            this.jobCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed.", e);
            throw new NiubiException(e);
        }
    }

    @Override
    protected synchronized void doExit() {
        try {
            if (nodeCache != null) {
                nodeCache.close();
            }
            LoggerHelper.info("node cache has been closed.");
        } catch (Throwable e) {
            LoggerHelper.warn("node cache close failed.", e);
        }
        schedulerManager.shutdown();
        LoggerHelper.info("all scheduler has been shutdown.");
        masterSlaveApiFactory.nodeApi().deleteNode(nodePath);
        LoggerHelper.info(getNodeIp() + " has been deleted.");
        leaderSelector.close();
        LoggerHelper.info("leaderSelector has been closed.");
        try {
            if (jobCache != null) {
                jobCache.close();
            }
            LoggerHelper.info("job cache has been closed.");
        } catch (Throwable e) {
            LoggerHelper.error("job cache close failed.", e);
        }
        client.close();
        LoggerHelper.info("zk client has been closed.");
    }

    /**
     * 释放节点的所有Job.
     *
     * @param nodePath 节点path.
     * @param nodeData 节点数据.
     */
    private void releaseJobs(String nodePath, MasterSlaveNodeData.Data nodeData) {
        if (ListHelper.isEmpty(nodeData.getJobPaths())) {
            return;
        }
        for (String path : nodeData.getJobPaths()) {
            MasterSlaveJobData.Data data = masterSlaveApiFactory.jobApi().getJob(path).getData();
            if (this.nodePath.equals(nodePath)) {
                schedulerManager.shutdown(data.getGroupName(), data.getJobName());
            }
            data.release();
            masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    private class MasterSlaveLeadershipSelectorListener extends AbstractLeadershipSelectorListener {

        @Override
        public void acquireLeadership() throws Exception {
            checkUnavailableNode();
            MasterSlaveNodeData masterSlaveNodeData = masterSlaveApiFactory.nodeApi().getNode(nodePath);
            masterSlaveNodeData.getData().setNodeState("Master");
            masterSlaveApiFactory.nodeApi().updateNode(nodePath, masterSlaveNodeData.getData());
            LoggerHelper.info(getNodeIp() + " has been updated. [" + masterSlaveNodeData.getData() + "]");
            nodeCache.start();
        }

        /**
         * 检查失效的节点,并且释放失效节点的Job.
         */
        private void checkUnavailableNode() {
            List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
            List<String> availableNodes = new ArrayList<>();
            if (!ListHelper.isEmpty(masterSlaveNodeDataList)) {
                availableNodes.addAll(masterSlaveNodeDataList.stream().map(MasterSlaveNodeData::getPath).collect(Collectors.toList()));
            }
            List<MasterSlaveJobData> masterSlaveJobDataList = masterSlaveApiFactory.jobApi().getAllJobs();
            if (!ListHelper.isEmpty(masterSlaveJobDataList)) {
                for (MasterSlaveJobData masterSlaveJobData : masterSlaveJobDataList) {
                    MasterSlaveJobData.Data data = masterSlaveJobData.getData();
                    if (!availableNodes.contains(data.getNodePath())) {
                        data.release();
                        masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                    }
                }
            }
        }

        @Override
        public void relinquishLeadership() {
            try {
                if (nodeCache != null) {
                    nodeCache.close();
                }
                LoggerHelper.info("node cache has been closed.");
            } catch (Throwable e) {
                LoggerHelper.warn("node cache close failed.", e);
            }
            if (client.getState() == CuratorFrameworkState.STARTED) {
                MasterSlaveNodeData.Data nodeData = new MasterSlaveNodeData.Data(getNodeIp());
                releaseJobs(nodePath, nodeData);
                nodeData.setNodeState("Slave");
                masterSlaveApiFactory.nodeApi().updateNode(nodePath, nodeData);
            }
            LoggerHelper.info("clear node successfully.");
        }

    }

    private class NodeCacheListener implements PathChildrenCacheListener {

        @Override
        public synchronized void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
            AssertHelper.isTrue(isJoined(), "illegal state .");
            //对Master权限进行双重检查
            if (!leaderSelector.hasLeadership()) {
                return;
            }
            if (EventHelper.isChildRemoveEvent(event)) {
                MasterSlaveNodeData masterSlaveNodeData = new MasterSlaveNodeData(event.getData().getPath(), event.getData().getData());
                releaseJobs(masterSlaveNodeData.getPath(), masterSlaveNodeData.getData());
            }
        }

    }

    private class JobCacheListener implements PathChildrenCacheListener {

        @Override
        public synchronized void childEvent(CuratorFramework clientInner, PathChildrenCacheEvent event) throws Exception {
            AssertHelper.isTrue(isJoined(), "illegal state .");
            if (!EventHelper.isChildModifyEvent(event)) {
                return;
            }
            MasterSlaveJobData jobData = new MasterSlaveJobData(event.getData());
            if (StringHelper.isEmpty(jobData.getData().getJobOperation())) {
                return;
            }
            MasterSlaveJobData.Data data = jobData.getData();
            if (data.isUnknownOperation()) {
                return;
            }
            boolean hasLeadership = leaderSelector != null && leaderSelector.hasLeadership();
            if (hasLeadership && StringHelper.isEmpty(data.getNodePath())) {
                //如果当前有操作的话,就等待3秒
                if (checkNotExecuteOperation()) {
                    try {
                        Thread.sleep(3000);
                    } catch (Throwable e) {
                        //ignored
                    }
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                    return;
                }
                List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
                if (ListHelper.isEmpty(masterSlaveNodeDataList)) {
                    data.operateFailed("there is not any one node live.");
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                    return;
                }
                Collections.sort(masterSlaveNodeDataList);
                data.setNodePath(masterSlaveNodeDataList.get(0).getPath());
                masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                return;
            }
            if (hasLeadership) {
                //检查任务被分配的节点是否有效
                List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
                boolean nodeIsLive = false;
                for (MasterSlaveNodeData masterSlaveNodeData : masterSlaveNodeDataList) {
                    if (masterSlaveNodeData.getPath().equals(data.getNodePath())) {
                        nodeIsLive = true;
                        break;
                    }
                }
                if (!nodeIsLive) {
                    data.clearNodePath();
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                }
            }
            //如果任务已经被分配到该节点,则在当前节点进行Job操作.
            if (EventHelper.isChildUpdateEvent(event) && nodePath.equals(data.getNodePath())) {
                MasterSlaveNodeData.Data nodeData;
                try {
                    nodeData = masterSlaveApiFactory.nodeApi().getNode(nodePath).getData();
                } catch (Throwable e) {
                    LoggerHelper.error("node [" + nodePath + "] not exists.");
                    data.clearNodePath();
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                    return;
                }
                executeOperation(nodeData, jobData);
                return;
            }
        }

        private boolean checkNotExecuteOperation() {
            List<MasterSlaveJobData> masterSlaveJobDataList = masterSlaveApiFactory.jobApi().getAllJobs();
            if (ListHelper.isEmpty(masterSlaveJobDataList)) {
                return false;
            }
            for (MasterSlaveJobData masterSlaveJobData : masterSlaveJobDataList) {
                boolean hasOperation = !StringHelper.isEmpty(masterSlaveJobData.getData().getJobOperation());
                boolean assigned = !StringHelper.isEmpty(masterSlaveJobData.getData().getNodePath());
                if (hasOperation && assigned) {
                    return true;
                }
            }
            return false;
        }

        private void executeOperation(MasterSlaveNodeData.Data nodeData, MasterSlaveJobData jobData) {
            MasterSlaveJobData.Data data = jobData.getData();
            try {
                if (data.isStart() || data.isRestart()) {
                    schedulerManager.startupManual(downloadJarFile(data.getJarFileName()), data.getPackagesToScan(), data.isSpring(), data.getGroupName(), data.getJobName(), data.getJobCron(), data.getMisfirePolicy());
                    if (data.isStart()) {
                        nodeData.addJobPath(jobData.getPath());
                    }
                    data.setJobState("Startup");
                } else {
                    schedulerManager.shutdown(data.getGroupName(), data.getJobName());
                    nodeData.removeJobPath(jobData.getPath());
                    data.clearNodePath();
                    data.setJobState("Pause");
                }
                data.operateSuccess();
                masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                masterSlaveApiFactory.nodeApi().updateNode(nodePath, nodeData);
            } catch (Throwable e) {
                LoggerHelper.error("handle operation failed. " + data, e);
                data.operateFailed(ExceptionHelper.getStackTrace(e, true));
                masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
            }
        }

    }

}
