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
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scheduler.DefaultManualScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.ManualScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;
import com.zuoxiaolong.niubi.job.scheduler.node.Node;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 集群Job节点抽象类,封装了节点状态的切换,子类只需要实现自己加入和退出集群的方法.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 *
 * @see MasterSlaveNode
 * @see StandbyNode
 */
public abstract class AbstractClusterJobNode extends AbstractNode implements Node {

    protected AtomicReference<State> state;

    protected ManualScheduleManager schedulerManager;

    public AbstractClusterJobNode() {
        super(Bootstrap.properties());
        this.schedulerManager = new DefaultManualScheduleManager(Bootstrap.properties());
        this.state = new AtomicReference<>();
        this.state.set(State.LATENT);
    }

    protected enum State { LATENT, JOINED, EXITED}

    protected boolean isJoined() {
        return this.state.get() == State.JOINED;
    }

    protected String downloadJarFile(String jarFileName) {
        String jarFilePath;
        try {
            jarFilePath = JarFileHelper.downloadJarFile(Bootstrap.getJobDir(), Bootstrap.getJarUrl(jarFileName));
        } catch (IOException e) {
            LoggerHelper.error("download jar file failed. [" + jarFileName + "]", e);
            throw new NiubiException(e);
        }
        return jarFilePath;
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

    /**
     * Master节点抽象监听器.
     * 本抽象类封装了获取Master权限和失去Master权限时线程的挂起控制
     */
    protected abstract class AbstractLeadershipSelectorListener implements LeaderSelectorListener {

        private final AtomicInteger leaderCount = new AtomicInteger();

        private Object mutex = new Object();

        public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
            LoggerHelper.info(getNodeIp() + " is now the leader ,and has been leader " + this.leaderCount.getAndIncrement() + " time(s) before.");
            boolean isJoined = isJoined();
            try {
                if (isJoined) {
                    acquireLeadership();
                }
            } catch (Throwable e) {
                relinquishLeadership();
                LoggerHelper.warn(getNodeIp() + " startup failed,relinquish leadership.", e);
                return;
            }
            try {
                synchronized (mutex) {
                    mutex.wait();
                }
            } catch (InterruptedException e) {
                LoggerHelper.info(getNodeIp() + " has been interrupted.");
            }
        }

        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            LoggerHelper.info(getNodeIp() + " state has been changed [" + newState + "]");
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
