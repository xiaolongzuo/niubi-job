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
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * master-slave mode.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class MasterSlaveNode extends AbstractRemoteJobNode {

    private CuratorFramework client;

    private final LeaderSelector leaderSelector;

    private InterProcessLock initLock;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    private MasterSlaveApiFactory masterSlaveApiFactory;

    private String nodePath;

    private PathChildrenCache jobCache;

    private PathChildrenCache nodeCache;

    public MasterSlaveNode() {
        this.client = CuratorFrameworkFactory.newClient(Bootstrap.getZookeeperAddresses(), retryPolicy);
        this.client.start();

        this.masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);

        this.nodePath = this.masterSlaveApiFactory.nodeApi().saveNode(new MasterSlaveNodeData.Data(getIp()));

        this.nodeCache = new PathChildrenCache(client, PathHelper.getParentPath(masterSlaveApiFactory.pathApi().getNodePath()), true);
        this.nodeCache.getListenable().addListener(createNodeCacheListener());

        this.jobCache = new PathChildrenCache(client, masterSlaveApiFactory.pathApi().getJobPath(), true);
        this.jobCache.getListenable().addListener(createJobCacheListener());

        this.leaderSelector = new LeaderSelector(client, masterSlaveApiFactory.pathApi().getSelectorPath(), createLeaderSelectorListener());
        leaderSelector.autoRequeue();

        initLock = new InterProcessMutex(client, masterSlaveApiFactory.pathApi().getInitLockPath());
        try {
            initLock.acquire();
            LoggerHelper.info("get init lock... begin init jobs.");
            initJobs();
            LoggerHelper.info("init jobs successfully.");
        } catch (Exception e) {
            throw new NiubiException(e);
        } finally {
            try {
                initLock.release();
            } catch (Exception e) {
                throw new NiubiException(e);
            }
        }
    }

    private void initJobs() {
        List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
        if (ListHelper.isEmpty(masterSlaveNodeDataList) || masterSlaveNodeDataList.size() > 1) {
            return;
        }
        MasterSlaveNodeData masterSlaveNodeData = masterSlaveNodeDataList.get(0);
        if (!nodePath.equals(masterSlaveNodeData.getPath())) {
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

    private PathChildrenCacheListener createNodeCacheListener() {
        return new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (!leaderSelector.hasLeadership()) {
                    return;
                }
                if (EventHelper.isChildRemoveEvent(event)) {
                    MasterSlaveNodeData masterSlaveNodeData = new MasterSlaveNodeData(event.getData().getPath(), event.getData().getData());
                    releaseJobs(masterSlaveNodeData.getPath(), masterSlaveNodeData.getData());
                }
            }
        };
    }

    private void releaseJobs(String nodePath, MasterSlaveNodeData.Data nodeData) {
        if (ListHelper.isEmpty(nodeData.getJobPaths())) {
            return;
        }
        for (String path : nodeData.getJobPaths()) {
            MasterSlaveJobData.Data data = masterSlaveApiFactory.jobApi().getJob(path).getData();
            if (this.nodePath.equals(nodePath)) {
                getContainer(data.getJarFileName(), data.getPackagesToScan(), data.isSpring()).schedulerManager().shutdown(data.getGroupName(), data.getJobName());
            }
            data.release();
            masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    private LeaderSelectorListener createLeaderSelectorListener() {
        return new LeaderSelectorListener() {

            private final AtomicInteger leaderCount = new AtomicInteger();

            private Object mutex = new Object();

            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                LoggerHelper.info(getIp() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
                try {
                    synchronized (mutex) {
                        checkUnavailableNode();
                        MasterSlaveNodeData masterSlaveNodeData = masterSlaveApiFactory.nodeApi().getNode(nodePath);
                        masterSlaveNodeData.getData().setState("Master");
                        masterSlaveApiFactory.nodeApi().updateNode(nodePath, masterSlaveNodeData.getData());
                        LoggerHelper.info(getIp() + " has been updated. [" + masterSlaveNodeData.getData() + "]");
                        mutex.wait();
                    }
                } catch (Exception e) {
                    LoggerHelper.info(getIp() + " startup failed,relinquish leadership.");
                } finally {
                    LoggerHelper.info(getIp() + " relinquishing leadership.");
                }
            }

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

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getIp() + " state change [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        MasterSlaveNodeData.Data nodeData = new MasterSlaveNodeData.Data(getIp());
                        releaseJobs(nodePath, nodeData);
                        nodeData.setState("Slave");
                        masterSlaveApiFactory.nodeApi().updateNode(nodePath, nodeData);
                        mutex.notify();
                    }
                }
            }

        };
    }

    private PathChildrenCacheListener createJobCacheListener() {
        return new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework clientInner, PathChildrenCacheEvent event) throws Exception {
                if (!EventHelper.isChildModifyEvent(event)) {
                    return;
                }
                MasterSlaveJobData jobData = new MasterSlaveJobData(event.getData());
                if (StringHelper.isEmpty(jobData.getData().getOperation())) {
                    return;
                }
                MasterSlaveJobData.Data data = jobData.getData();
                if (data.isUnknownOperation()) {
                    return;
                }
                boolean hasLeadership = leaderSelector != null && leaderSelector.hasLeadership();
                if (hasLeadership && StringHelper.isEmpty(data.getNodePath())) {
                    //if has operation, wait a moment.
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
                    //check weigher node path
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
                //if the job has been assigned to this node, then execute.
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
        };
    }

    private boolean checkNotExecuteOperation() {
        List<MasterSlaveJobData> masterSlaveJobDataList = masterSlaveApiFactory.jobApi().getAllJobs();
        if (ListHelper.isEmpty(masterSlaveJobDataList)) {
            return false;
        }
        for (MasterSlaveJobData masterSlaveJobData : masterSlaveJobDataList) {
            boolean hasOperation = !StringHelper.isEmpty(masterSlaveJobData.getData().getOperation());
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
                Container container = getContainer(data.getJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.schedulerManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                if (data.isStart()) {
                    nodeData.addJobPath(jobData.getPath());
                }
                data.setState("Startup");
            } else {
                Container container = getContainer(data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.schedulerManager().shutdown(data.getGroupName(), data.getJobName());
                nodeData.removeJobPath(jobData.getPath());
                data.clearNodePath();
                data.setState("Pause");
            }
            data.operateSuccess();
            masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
            masterSlaveApiFactory.nodeApi().updateNode(nodePath, nodeData);
        } catch (Throwable e) {
            LoggerHelper.error("handle operation failed. " + data, e);
            data.operateFailed(e.getClass().getName() + ":" + e.getMessage());
            masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    @Override
    public void join() {
        synchronized (getContainerCache()) {
            leaderSelector.start();
            try {
                this.jobCache.start();
            } catch (Exception e) {
                LoggerHelper.error("path children path start failed.", e);
                throw new NiubiException(e);
            }
            try {
                this.nodeCache.start();
            } catch (Exception e) {
                LoggerHelper.error("path children path start failed.", e);
                throw new NiubiException(e);
            }
        }
    }

    @Override
    public void exit() {
        synchronized (getContainerCache()) {
            leaderSelector.close();
            try {
                jobCache.close();
            } catch (IOException e) {
                LoggerHelper.error("path children path close failed.", e);
                throw new NiubiException(e);
            }
            try {
                nodeCache.close();
            } catch (IOException e) {
                LoggerHelper.error("path children path close failed.", e);
                throw new NiubiException(e);
            }
            LoggerHelper.info("selector and cache has been closed.");
            for (String key : getContainerCache().keySet()) {
                getContainerCache().get(key).schedulerManager().shutdown();
            }
            LoggerHelper.info("containers has been shutdown.");
            MasterSlaveNodeData nodeData = masterSlaveApiFactory.nodeApi().getNode(nodePath);
            releaseJobs(nodePath, nodeData.getData());
            LoggerHelper.info("jobs has been released.");
            client.close();
            LoggerHelper.info("zk client has been closed.");
        }
    }

}
