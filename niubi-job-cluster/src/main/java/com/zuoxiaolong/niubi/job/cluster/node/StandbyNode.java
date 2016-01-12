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

import com.zuoxiaolong.niubi.job.cluster.config.ClusterConfiguration;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.node.AbstractNode;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 主备模式实现
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class StandbyNode extends AbstractNode {

    public static final String LEADER_PATH = "/fairnodeleaderpath";

    private final LeaderSelector leaderSelector;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 6);

    private CuratorFramework client;

    public StandbyNode() {
        this(new ClusterConfiguration());
    }

    public StandbyNode(ClusterConfiguration configuration) {
        super(configuration);
        this.client = CuratorFrameworkFactory.newClient(configuration.getConnectString(), retryPolicy);
        this.client.start();
        this.leaderSelector = new LeaderSelector(client, LEADER_PATH, createLeaderSelectorListener());
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
                        getContainer().getScheduleManager().startup();
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
                        getContainer().getScheduleManager().shutdown();
                        LoggerHelper.info(getName() + " has been shutdown");
                        mutex.notify();
                    }
                }
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
