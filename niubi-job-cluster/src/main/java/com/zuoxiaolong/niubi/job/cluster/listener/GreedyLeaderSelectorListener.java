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

package com.zuoxiaolong.niubi.job.cluster.listener;

import com.zuoxiaolong.niubi.job.cluster.node.Node;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * greedy模式监听器,当获取到leader权限时,一直运行到连接断开才放弃leader权限
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 06:03
 */
public class GreedyLeaderSelectorListener extends LeaderSelectorListenerAdapter {

    private Node node;

    private final AtomicInteger leaderCount = new AtomicInteger();

    private Object mutex = new Object();

    public GreedyLeaderSelectorListener(Node node) {
        this.node = node;
    }

    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        LoggerHelper.info(this.node.getName() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
        try {
            synchronized (mutex) {
                this.node.getContainer().getScheduleManager().startup();
                mutex.wait();
            }
        } catch (Exception e) {
            LoggerHelper.info(this.node.getName() + " startup failed,relinquish leadership.");
        } finally {
            LoggerHelper.info(this.node.getName() + " relinquishing leadership.");
        }
    }

    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        LoggerHelper.info(this.node.getName() + " state has been changed [" + newState + "]");
        if (!newState.isConnected()) {
            synchronized (mutex) {
                LoggerHelper.info(this.node.getName() + "'s connection has been un-connected");
                this.node.getContainer().getScheduleManager().shutdown();
                LoggerHelper.info(this.node.getName() + " has been shutdown");
                mutex.notify();
            }
        }
    }

}
