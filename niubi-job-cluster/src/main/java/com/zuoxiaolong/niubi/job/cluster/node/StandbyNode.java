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

import com.zuoxiaolong.niubi.job.api.ApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.ApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.api.data.NodeData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
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
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主备模式实现
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class StandbyNode extends AbstractRemoteJobNode {

    private final LeaderSelector leaderSelector;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    private CuratorFramework client;

    private PathChildrenCache pathChildrenCache;

    private ApiFactory apiFactory;

    private String zookeeperAddresses;

    private String jarRepertoryUrl;

    private String nodePath;

    public StandbyNode(String zookeeperAddresses, String jarRepertoryUrl, String[] propertiesFileNames) {
        super(propertiesFileNames);
        this.zookeeperAddresses = zookeeperAddresses;
        this.jarRepertoryUrl = StringHelper.appendSlant(jarRepertoryUrl);
        this.client = CuratorFrameworkFactory.newClient(this.zookeeperAddresses, retryPolicy);
        this.client.start();
        this.apiFactory = new ApiFactoryImpl(client);
        this.nodePath = this.apiFactory.nodeApi().createStandbyNode(new NodeData.Data(getIp()));
        this.pathChildrenCache = new PathChildrenCache(client, apiFactory.pathApi().getStandbyJobPath(), true);
        this.pathChildrenCache.getListenable().addListener(createPathChildrenCacheListener());
        try {
            this.pathChildrenCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed.", e);
            throw new NiubiException(e);
        }
        this.leaderSelector = new LeaderSelector(client, apiFactory.pathApi().getStandbyMasterPath(), createLeaderSelectorListener());
        System.out.println(this.leaderSelector.getId());
        leaderSelector.autoRequeue();
    }

    private LeaderSelectorListener createLeaderSelectorListener() {

        return new LeaderSelectorListenerAdapter() {

            private final AtomicInteger leaderCount = new AtomicInteger();

            private Object mutex = new Object();

            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                LoggerHelper.info(getIp() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
                try {
                    synchronized (mutex) {
                        NodeData.Data nodeData = new NodeData.Data(getIp());
                        int runningJobCount = startupJobs();
                        updateNodeData(nodeData, runningJobCount);
                        mutex.wait();
                    }
                } catch (Exception e) {
                    LoggerHelper.info(getIp() + " startup failed,relinquish leadership.");
                } finally {
                    LoggerHelper.info(getIp() + " relinquishing leadership.");
                }
            }

            private Integer startupJobs() {
                List<JobData> jobDataList = apiFactory.jobApi().selectAllStandbyJobs();
                int runningJobCount = 0;
                for (JobData jobData : jobDataList) {
                    try {
                        JobData.Data data = jobData.getData();
                        if ("Startup".equals(data.getState())) {
                            Container container = getContainer(jarRepertoryUrl + jobData.getData().getJarFileName(), jobData.getData().getPackagesToScan(), jobData.getData().isSpring());
                            container.scheduleManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                            runningJobCount++;
                        }
                    } catch (Exception e) {
                        LoggerHelper.error("start jar failed [" + jobData.getPath() + "]", e);
                    }
                }
                return runningJobCount;
            }

            private void updateNodeData(NodeData.Data data, Integer runningJobCount) {
                data.setRunningJobCount(runningJobCount);
                data.setState("Master");
                apiFactory.nodeApi().updateStandbyNode(nodePath, data);
                LoggerHelper.info(getIp() + " has been updated. [" + data + "]");
            }

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getIp() + " state has been changed [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        LoggerHelper.info(getIp() + "'s connection has been un-connected");
                        for (Container container : getContainerCache().values()) {
                            container.scheduleManager().shutdown();
                        }
                        NodeData.Data data = new NodeData.Data(getIp());
                        apiFactory.nodeApi().updateStandbyNode(nodePath, data);
                        LoggerHelper.info(getIp() + " has been shutdown. [" + data + "]");
                        mutex.notify();
                    }
                }
            }

        };
    }

    public PathChildrenCacheListener createPathChildrenCacheListener() {
        return new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                boolean hasLeadership = leaderSelector != null && leaderSelector.hasLeadership();
                if (!hasLeadership) {
                    return;
                }
                if (!EventHelper.isChildModifyEvent(event)) {
                    return;
                }
                JobData jobData = new JobData(event.getData());
                if (StringHelper.isEmpty(jobData.getData().getOperation())) {
                    return;
                }
                JobData.Data data = jobData.getData();
                if (data.isUnknownOperation()) {
                    return;
                }
                NodeData.Data nodeData = apiFactory.nodeApi().selectStandbyNode(nodePath).getData();
                StandbyNode.this.executeOperation(nodeData, data);
            }
        };
    }

    private void executeOperation(NodeData.Data nodeData, JobData.Data data) {
        try {
            if (data.isStart() || data.isRestart()) {
                if (data.isRestart()) {
                    Container container = getContainer(jarRepertoryUrl + data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                    container.scheduleManager().shutdown(data.getGroupName(), data.getJobName());
                    nodeData.setRunningJobCount(nodeData.getRunningJobCount() - 1);
                }
                Container container = getContainer(jarRepertoryUrl + data.getJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.scheduleManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                nodeData.setRunningJobCount(nodeData.getRunningJobCount() + 1);
                data.setState("Startup");
            } else {
                Container container = getContainer(jarRepertoryUrl + data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.scheduleManager().shutdown(data.getGroupName(), data.getJobName());
                nodeData.setRunningJobCount(nodeData.getRunningJobCount() - 1);
                data.setState("Pause");
            }
            data.operateSuccess();
            apiFactory.jobApi().updateStandbyJob(data.getGroupName(), data.getJobName(), data);
            apiFactory.nodeApi().updateStandbyNode(nodePath, nodeData);
        } catch (Throwable e) {
            LoggerHelper.error("handle operation failed. " + data, e);
            data.operateFailed(e.getClass().getName() + ":" + e.getMessage());
            apiFactory.jobApi().updateStandbyJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    public synchronized void join() {
        leaderSelector.start();
    }

    public synchronized void exit() {
        leaderSelector.close();
        this.client.close();
    }

}
