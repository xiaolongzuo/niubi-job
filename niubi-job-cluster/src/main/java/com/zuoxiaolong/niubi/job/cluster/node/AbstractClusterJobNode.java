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

import com.zuoxiaolong.niubi.job.cluster.startup.Bootstrap;
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scheduler.DefaultSchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.SchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;
import com.zuoxiaolong.niubi.job.scheduler.node.Node;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractClusterJobNode extends AbstractNode implements Node {

    protected AtomicReference<State> state;

    protected SchedulerManager schedulerManager;

    public AbstractClusterJobNode() {
        super(Bootstrap.properties());
        this.schedulerManager = new DefaultSchedulerManager(Bootstrap.properties());
        this.state = new AtomicReference<>();
        this.state.set(State.LATENT);
    }

    protected enum State { LATENT, JOINED, EXITED}

    protected boolean isJoined() {
        return this.state.get() == State.JOINED;
    }

    @Override
    public void join() {
        AssertHelper.isTrue(state.compareAndSet(State.LATENT, State.JOINED), "illegal state .");
        doJoin();
    }

    @Override
    public void exit() {
        AssertHelper.isTrue(state.compareAndSet(State.JOINED, State.EXITED), "illegal state .");
        doExit();
    }

    protected abstract void doJoin();

    protected abstract void doExit();

    protected abstract class AbstractLeadershipSelectorListener implements LeaderSelectorListener {

        private final AtomicInteger leaderCount = new AtomicInteger();

        private Object mutex = new Object();

        public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
            LoggerHelper.info(getIp() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
            boolean isJoined = isJoined();
            try {
                if (isJoined) {
                    acquireLeadership();
                }
            } catch (Throwable e) {
                relinquishLeadership();
                LoggerHelper.warn(getIp() + " startup failed,relinquish leadership.", e);
                return;
            }
            try {
                synchronized (mutex) {
                    mutex.wait();
                }
            } catch (InterruptedException e) {
                LoggerHelper.info(getIp() + " has been interrupted.");
            }
        }

        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            LoggerHelper.info(getIp() + " state has been changed [" + newState + "]");
            if (!newState.isConnected()) {
                relinquishLeadership();
                synchronized (mutex) {
                    mutex.notify();
                }
            }
        }

        public abstract void acquireLeadership() throws Exception;

        public abstract void relinquishLeadership();

    }

}
