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

import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractLeadershipSelectorListener implements LeaderSelectorListener {

    private final AtomicInteger leaderCount = new AtomicInteger();

    private Object mutex = new Object();

    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        LoggerHelper.info(getIdentifier() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
        try {
            acquireLeadership();
        } catch (Throwable e) {
            relinquishLeadership();
            LoggerHelper.warn(getIdentifier() + " startup failed,relinquish leadership.", e);
            return;
        }
        try {
            synchronized (mutex) {
                mutex.wait();
            }
        } catch (InterruptedException e) {
            LoggerHelper.info(getIdentifier() + " has been interrupted.");
        }
    }

    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        LoggerHelper.info(getIdentifier() + " state has been changed [" + newState + "]");
        if (!newState.isConnected()) {
            relinquishLeadership();
            synchronized (mutex) {
                mutex.notify();
            }
        }
    }

    public abstract String getIdentifier();

    public abstract void acquireLeadership() throws Exception;

    public abstract void relinquishLeadership();

}
