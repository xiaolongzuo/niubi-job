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
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
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
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主从模式实现
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class MasterSlaveNode extends AbstractRemoteJobNode {

    private CuratorFramework client;

    private final LeaderSelector leaderSelector;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    private MasterSlaveApiFactory masterSlaveApiFactory;

    private String nodePath;

    private PathChildrenCache jobCache;

    private PathChildrenCache nodeCache;

    public MasterSlaveNode(String zookeeperAddresses, String jarRepertoryUrl, String[] propertiesFileNames) {
        super(jarRepertoryUrl, propertiesFileNames);
        this.client = CuratorFrameworkFactory.newClient(zookeeperAddresses, retryPolicy);
        this.client.start();

        this.masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);

        this.nodePath = this.masterSlaveApiFactory.nodeApi().saveNode(new MasterSlaveNodeData.Data(getIp()));
        this.nodeCache = new PathChildrenCache(client, PathHelper.getParentPath(masterSlaveApiFactory.pathApi().getNodePath()), true);
        this.nodeCache.getListenable().addListener(createNodeCacheListener());
        try {
            this.nodeCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed.", e);
            throw new NiubiException(e);
        }

        this.jobCache = new PathChildrenCache(client, masterSlaveApiFactory.pathApi().getJobPath(), true);
        this.jobCache.getListenable().addListener(createJobCacheListener());
        try {
            this.jobCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed.", e);
            throw new NiubiException(e);
        }

        this.leaderSelector = new LeaderSelector(client, masterSlaveApiFactory.pathApi().getSelectorPath(), createLeaderSelectorListener());
        leaderSelector.autoRequeue();
    }

    private PathChildrenCacheListener createNodeCacheListener() {
        return new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (!leaderSelector.hasLeadership()) {
                    return;
                }
                if (EventHelper.isChildRemoveEvent(event)) {
                    releaseJobs(event.getData().getPath());
                }
            }
        };
    }

    private void releaseJobs(String nodePath) {
        MasterSlaveNodeData masterSlaveNodeData = masterSlaveApiFactory.nodeApi().getNode(nodePath);
        for (String path : masterSlaveNodeData.getData().getJobPaths()) {
            MasterSlaveJobData.Data data = masterSlaveApiFactory.jobApi().getJob(path).getData();
            if (this.nodePath.equals(nodePath)) {
                getContainer(data.getJarFileName(), data.getPackagesToScan(), data.isSpring()).scheduleManager().shutdown(data.getGroupName(), data.getJobName());
            }
            data.clearNodePath();
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
                        MasterSlaveNodeData.Data nodeData = new MasterSlaveNodeData.Data(getIp());
                        nodeData.setState("Master");
                        masterSlaveApiFactory.nodeApi().updateNode(nodePath, nodeData);
                        LoggerHelper.info(getIp() + " has been updated. [" + nodeData + "]");
                        mutex.wait();
                    }
                } catch (Exception e) {
                    LoggerHelper.info(getIp() + " startup failed,relinquish leadership.");
                } finally {
                    LoggerHelper.info(getIp() + " relinquishing leadership.");
                }
            }

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getIp() + " state change [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        releaseJobs(nodePath);
                        MasterSlaveNodeData.Data nodeData = new MasterSlaveNodeData.Data(getIp());
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
                    List<MasterSlaveNodeData> masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
                    Collections.sort(masterSlaveNodeDataList);
                    data.setNodePath(masterSlaveNodeDataList.get(0).getPath());
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                    return;
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
                if (!EventHelper.isChildRemoveEvent(event)) {
                    //clear node path ,ready for next allocation
                    data.clearNodePath();
                    masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
                }
            }
        };
    }

    private void executeOperation(MasterSlaveNodeData.Data nodeData, MasterSlaveJobData jobData) {
        MasterSlaveJobData.Data data = jobData.getData();
        try {
            if (data.isStart() || data.isRestart()) {
                if (data.isRestart()) {
                    Container container = getContainer(data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                    container.scheduleManager().shutdown(data.getGroupName(), data.getJobName());
                    nodeData.removeJobPath(jobData.getPath());
                }
                Container container = getContainer(data.getJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.scheduleManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                nodeData.addJobPath(jobData.getPath());
                data.setState("Startup");
            } else {
                Container container = getContainer(data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.scheduleManager().shutdown(data.getGroupName(), data.getJobName());
                nodeData.setRunningJobCount(nodeData.getRunningJobCount() - 1);
                nodeData.removeJobPath(jobData.getPath());
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
        leaderSelector.start();
    }

    @Override
    public void exit() {
        leaderSelector.close();
        client.close();
    }

}
