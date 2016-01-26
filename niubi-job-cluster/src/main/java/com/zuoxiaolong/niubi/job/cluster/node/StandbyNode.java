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

import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.StandbyApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.api.data.StandbyNodeData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
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
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * standby mode.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StandbyNode extends AbstractRemoteJobNode {

    private final LeaderSelector leaderSelector;

    private InterProcessLock initLock;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    private CuratorFramework client;

    private PathChildrenCache jobCache;

    private StandbyApiFactory standbyApiFactory;

    private String nodePath;

    public StandbyNode() {
        this.client = CuratorFrameworkFactory.newClient(Bootstrap.getZookeeperAddresses(), retryPolicy);
        this.client.start();

        this.standbyApiFactory = new StandbyApiFactoryImpl(client);

        this.nodePath = this.standbyApiFactory.nodeApi().saveNode(new StandbyNodeData.Data(getIp()));

        this.jobCache = new PathChildrenCache(client, standbyApiFactory.pathApi().getJobPath(), true);
        this.jobCache.getListenable().addListener(createPathChildrenCacheListener());

        this.leaderSelector = new LeaderSelector(client, standbyApiFactory.pathApi().getSelectorPath(), createLeaderSelectorListener());
        leaderSelector.autoRequeue();

        initLock = new InterProcessMutex(client, standbyApiFactory.pathApi().getInitLockPath());
        try {
            initLock.acquire();
            initJobs();
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
        List<StandbyNodeData> standbyNodeDataList = standbyApiFactory.nodeApi().getAllNodes();
        if (ListHelper.isEmpty(standbyNodeDataList) || standbyNodeDataList.size() > 1) {
            return;
        }
        StandbyNodeData standbyNodeData = standbyNodeDataList.get(0);
        if (!nodePath.equals(standbyNodeData.getPath())) {
            return;
        }
        List<StandbyJobData> standbyJobDataList = standbyApiFactory.jobApi().getAllJobs();
        for (StandbyJobData standbyJobData : standbyJobDataList) {
            StandbyJobData.Data data = standbyJobData.getData();
            data.init();
            standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    private LeaderSelectorListener createLeaderSelectorListener() {

        return new LeaderSelectorListenerAdapter() {

            private final AtomicInteger leaderCount = new AtomicInteger();

            private Object mutex = new Object();

            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                LoggerHelper.info(getIp() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
                try {
                    synchronized (mutex) {
                        StandbyNodeData.Data nodeData = new StandbyNodeData.Data(getIp());
                        int runningJobCount = startupJobs();
                        nodeData.setRunningJobCount(runningJobCount);
                        nodeData.setState("Master");
                        standbyApiFactory.nodeApi().updateNode(nodePath, nodeData);
                        LoggerHelper.info(getIp() + " has been updated. [" + nodeData + "]");
                        mutex.wait();
                    }
                } catch (Exception e) {
                    LoggerHelper.info(getIp() + " startup failed,relinquish leadership.");
                } finally {
                    LoggerHelper.info(getIp() + " relinquishing leadership.");
                }
            }

            private Integer startupJobs() {
                List<StandbyJobData> standbyJobDataList = standbyApiFactory.jobApi().getAllJobs();
                int runningJobCount = 0;
                for (StandbyJobData standbyJobData : standbyJobDataList) {
                    try {
                        StandbyJobData.Data data = standbyJobData.getData();
                        if ("Startup".equals(data.getState())) {
                            Container container = getContainer(standbyJobData.getData().getJarFileName(), standbyJobData.getData().getPackagesToScan(), standbyJobData.getData().isSpring());
                            container.schedulerManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                            runningJobCount++;
                        }
                    } catch (Exception e) {
                        LoggerHelper.error("start jar failed [" + standbyJobData.getPath() + "]", e);
                    }
                }
                return runningJobCount;
            }

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getIp() + " state has been changed [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        LoggerHelper.info(getIp() + "'s connection has been un-connected");
                        for (Container container : getContainerCache().values()) {
                            container.schedulerManager().shutdown();
                        }
                        StandbyNodeData.Data data = new StandbyNodeData.Data(getIp());
                        standbyApiFactory.nodeApi().updateNode(nodePath, data);
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
                StandbyJobData standbyJobData = new StandbyJobData(event.getData());
                if (StringHelper.isEmpty(standbyJobData.getData().getOperation())) {
                    return;
                }
                StandbyJobData.Data data = standbyJobData.getData();
                if (data.isUnknownOperation()) {
                    return;
                }
                StandbyNodeData.Data nodeData = standbyApiFactory.nodeApi().getNode(nodePath).getData();
                executeOperation(nodeData, data);
            }
        };
    }

    private void executeOperation(StandbyNodeData.Data nodeData, StandbyJobData.Data data) {
        try {
            if (data.isStart() || data.isRestart()) {
                Container container = getContainer(data.getJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.schedulerManager().startupManual(data.getGroupName(), data.getJobName(), data.getCron(), data.getMisfirePolicy());
                if (data.isStart()) {
                    nodeData.increment();
                }
                data.setState("Startup");
            } else {
                Container container = getContainer(data.getOriginalJarFileName(), data.getPackagesToScan(), data.isSpring());
                container.schedulerManager().shutdown(data.getGroupName(), data.getJobName());
                nodeData.decrement();
                data.setState("Pause");
            }
            data.operateSuccess();
            standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
            standbyApiFactory.nodeApi().updateNode(nodePath, nodeData);
        } catch (Throwable e) {
            LoggerHelper.error("handle operation failed. " + data, e);
            data.operateFailed(e.getClass().getName() + ":" + e.getMessage());
            standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    public synchronized void join() {
        leaderSelector.start();
        try {
            this.jobCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed.", e);
            throw new NiubiException(e);
        }
    }

    public synchronized void exit() {
        leaderSelector.close();
        try {
            jobCache.close();
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        client.close();
    }

}
