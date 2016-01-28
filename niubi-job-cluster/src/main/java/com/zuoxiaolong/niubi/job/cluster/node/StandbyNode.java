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
import com.zuoxiaolong.niubi.job.core.helper.*;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
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
import java.util.List;

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

        this.initLock = new InterProcessMutex(client, standbyApiFactory.pathApi().getInitLockPath());
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

        this.nodePath = standbyApiFactory.nodeApi().saveNode(new StandbyNodeData.Data(getIp()));

        this.jobCache = new PathChildrenCache(client, standbyApiFactory.pathApi().getJobPath(), true);
        this.jobCache.getListenable().addListener(new JobCacheListener());

        this.leaderSelector = new LeaderSelector(client, standbyApiFactory.pathApi().getSelectorPath(), new StandbyLeadershipSelectorListener());
        this.leaderSelector.autoRequeue();

    }

    private void initJobs() {
        List<StandbyNodeData> standbyNodeDataList = standbyApiFactory.nodeApi().getAllNodes();
        if (!ListHelper.isEmpty(standbyNodeDataList)) {
            return;
        }
        List<StandbyJobData> standbyJobDataList = new ArrayList<>();
        try {
            standbyJobDataList = standbyApiFactory.jobApi().getAllJobs();
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
        for (StandbyJobData standbyJobData : standbyJobDataList) {
            StandbyJobData.Data data = standbyJobData.getData();
            data.init();
            standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    public synchronized void doJoin() {
        leaderSelector.start();
    }

    public synchronized void doExit() {
        try {
            if (jobCache != null) {
                jobCache.close();
            }
            LoggerHelper.info("job cache has been closed.");
        } catch (Throwable e) {
            LoggerHelper.error("job cache close failed.", e);
        }
        shutdownAllScheduler();
        LoggerHelper.info("all scheduler has been shutdown.");
        standbyApiFactory.nodeApi().deleteNode(nodePath);
        LoggerHelper.info(getIp() + " has been deleted.");
        leaderSelector.close();
        LoggerHelper.info("leaderSelector has been closed.");
        client.close();
    }

    private class StandbyLeadershipSelectorListener extends AbstractLeadershipSelectorListener {

        @Override
        public void acquireLeadership() throws Exception {
            AssertHelper.isTrue(isJoined(), "illegal state .");
            StandbyNodeData.Data nodeData = new StandbyNodeData.Data(getIp());
            int runningJobCount = startupJobs();
            nodeData.setRunningJobCount(runningJobCount);
            nodeData.setState("Master");
            standbyApiFactory.nodeApi().updateNode(nodePath, nodeData);
            LoggerHelper.info(getIp() + " has been updated. [" + nodeData + "]");
            jobCache.start();
        }

        private Integer startupJobs() {
            List<StandbyJobData> standbyJobDataList = standbyApiFactory.jobApi().getAllJobs();
            int runningJobCount = 0;
            if (ListHelper.isEmpty(standbyJobDataList)) {
                return runningJobCount;
            }
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

        @Override
        public void relinquishLeadership() {
            try {
                if (jobCache != null) {
                    jobCache.close();
                }
                LoggerHelper.info("job cache has been closed.");
            } catch (Throwable e) {
                LoggerHelper.warn("job cache close failed.", e);
            }
            LoggerHelper.info("begin stop scheduler manager.");
            shutdownAllScheduler();
            if (client.getState() == CuratorFrameworkState.STARTED) {
                StandbyNodeData.Data data = new StandbyNodeData.Data(getIp());
                standbyApiFactory.nodeApi().updateNode(nodePath, data);
                LoggerHelper.info(getIp() + " has been shutdown. [" + data + "]");
            }
            LoggerHelper.info("clear node successfully.");
        }

    }

    private class JobCacheListener implements PathChildrenCacheListener {

        @Override
        public synchronized void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
            AssertHelper.isTrue(isJoined(), "illegal state .");
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
                data.operateFailed(ExceptionHelper.getStackTrace(e, true));
                standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
            }
        }

    }

}
