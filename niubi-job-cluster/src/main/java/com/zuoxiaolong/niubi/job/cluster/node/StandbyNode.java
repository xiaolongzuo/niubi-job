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
import com.zuoxiaolong.niubi.job.api.NodeApi;
import com.zuoxiaolong.niubi.job.api.PathApi;
import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.container.Container;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.core.node.AbstractRemoteJobNode;
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

    private PathApi pathApi;

    private NodeApi nodeApi;

    private String zookeeperAddresses;

    private String jarRepertoryUrl;

    public StandbyNode(String zookeeperAddresses, String jarRepertoryUrl) {
        this.zookeeperAddresses = zookeeperAddresses;
        this.jarRepertoryUrl = StringHelper.appendSlant(jarRepertoryUrl);
        this.client = CuratorFrameworkFactory.newClient(this.zookeeperAddresses, retryPolicy);
        this.client.start();
        this.pathApi = ApiFactory.instance().pathApi();
        this.nodeApi = ApiFactory.instance().nodeApi(client);
        this.pathChildrenCache = new PathChildrenCache(client, pathApi.getStandbyNodeJobJarPath(), true);
        this.pathChildrenCache.getListenable().addListener(createPathChildrenCacheListener());
        try {
            this.pathChildrenCache.start();
        } catch (Exception e) {
            LoggerHelper.error("path children path start failed." , e);
            throw new NiubiException(e);
        }
        this.leaderSelector = new LeaderSelector(client, pathApi.getStandbyNodeMasterPath(), createLeaderSelectorListener());
        leaderSelector.autoRequeue();
    }

    private LeaderSelectorListener createLeaderSelectorListener() {

        return new LeaderSelectorListenerAdapter() {

            private final AtomicInteger leaderCount = new AtomicInteger();

            private Object mutex = new Object();

            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                LoggerHelper.info(getName() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
                try {
                    synchronized (mutex) {
                        List<String> jobJarList = nodeApi.getStandbyNodeJobJarList();
                        for (String jarFileName : jobJarList) {
                            try {
                                getContainer(jarRepertoryUrl + jarFileName).getScheduleManager().startup();
                            } catch (Exception e) {
                                LoggerHelper.error("start jar failed [" + jarFileName + "]", e);
                            }
                        }
                        mutex.wait();
                    }
                } catch (Exception e) {
                    LoggerHelper.info(getName() + " startup failed,relinquish leadership.");
                } finally {
                    LoggerHelper.info(getName() + " relinquishing leadership.");
                }
            }

            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LoggerHelper.info(getName() + " state has been changed [" + newState + "]");
                if (!newState.isConnected()) {
                    synchronized (mutex) {
                        LoggerHelper.info(getName() + "'s connection has been un-connected");
                        for (Container container : getContainerCache().values()) {
                            container.getScheduleManager().shutdown();
                        }
                        LoggerHelper.info(getName() + " has been shutdown");
                        mutex.notify();
                    }
                }
            }

        };
    }

    public PathChildrenCacheListener createPathChildrenCacheListener() {
        return (curatorFramework, event) -> {
            boolean hasLeadership = leaderSelector != null && leaderSelector.hasLeadership();
            boolean isAddOrRemoveEvent = event!= null && (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED);
            if (!hasLeadership || !isAddOrRemoveEvent) {
                return;
            }
            String path = event.getData().getPath();
            path = path.substring(path.lastIndexOf("/") + 1);
            Container container = getContainer(path);
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
                container.getScheduleManager().startup();
            } else {
                container.getScheduleManager().shutdown();
            }
        };
    }

    public synchronized void join() {
        leaderSelector.start();
    }

    public synchronized void exit() {
        leaderSelector.close();
        this.client.close();
    }

}
