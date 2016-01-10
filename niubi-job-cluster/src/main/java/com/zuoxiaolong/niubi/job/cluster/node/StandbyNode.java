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

import com.zuoxiaolong.niubi.job.cluster.listener.GreedyLeaderSelectorListener;
import com.zuoxiaolong.niubi.job.core.config.Configuration;
import com.zuoxiaolong.niubi.job.core.container.Container;
import com.zuoxiaolong.niubi.job.core.container.DefaultContainer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 集群节点的curator实现,可以保证同一时间有且仅有一个节点在运行job
 * 该节点采取fair模式,当leadership出现问题时,按照启动顺序选为leadership
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 14:43
 */
public class StandbyNode implements Node {

    public static final String LEADER_PATH = "/fairnodeleaderpath";

    private final LeaderSelector leaderSelector;

    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 6);

    private String name;

    private Container container;

    private CuratorFramework client;

    public StandbyNode(Configuration configuration, String connectString) {
        try {
            this.name = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            //ignored
        }
        this.container = new DefaultContainer(configuration);
        this.client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        this.client.start();
        this.leaderSelector = new LeaderSelector(client, LEADER_PATH, new GreedyLeaderSelectorListener(this));
        leaderSelector.autoRequeue();
    }

    public Container getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public synchronized void join() {
        leaderSelector.start();
    }

    public synchronized void exit() {
        leaderSelector.close();
        this.client.close();
    }

}
