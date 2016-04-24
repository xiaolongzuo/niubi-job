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

package com.zuoxiaolong.niubi.job.api.curator;

import com.zuoxiaolong.niubi.job.api.*;
import com.zuoxiaolong.niubi.job.test.zookeeper.ZookeeperServerCluster;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.After;
import org.junit.Before;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public abstract class AbstractZookeeperServerTest {

    protected static MasterSlaveApiFactory masterSlaveApiFactory;

    protected static MasterSlaveJobApi masterSlaveJobApi;

    protected static MasterSlaveNodeApi masterSlaveNodeApi;

    protected static StandbyApiFactory standbyApiFactory;

    protected static StandbyJobApi standbyJobApi;

    protected static StandbyNodeApi standbyNodeApi;

    protected static CuratorFramework client;

    @Before
    public void setup() {
        ZookeeperServerCluster.startZookeeperCluster();
        client = CuratorFrameworkFactory.newClient("localhost:2182,localhost:3182,localhost:4182", new ExponentialBackoffRetry(1000, Integer.MAX_VALUE));
        client.start();
        masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);
        standbyApiFactory = new StandbyApiFactoryImpl(client);
        masterSlaveNodeApi = masterSlaveApiFactory.nodeApi();
        masterSlaveJobApi = masterSlaveApiFactory.jobApi();
        standbyNodeApi = standbyApiFactory.nodeApi();
        standbyJobApi = standbyApiFactory.jobApi();
    }

    @After
    public void teardown() {
        client.close();
        ZookeeperServerCluster.stopZookeeperCluster();
    }

}
