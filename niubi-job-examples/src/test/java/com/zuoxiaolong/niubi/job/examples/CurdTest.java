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

package com.zuoxiaolong.niubi.job.examples;

import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 * @author Xiaolong Zuo
 * @since 16/1/10 19:44
 */
public class CurdTest {

    @Test
    public void testCreate() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println(JsonHelper.toJson(event));
            }
        }).forPath("/nodecache/child1", "123".getBytes());

        Thread.sleep(10000);
    }

    @Test
    public void testCreate1() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.create().inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println(JsonHelper.toJson(event));
            }
        }).forPath("/nodecache/child1"));

        Thread.sleep(10000);
    }

    @Test
    public void testCreate2() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.create().withProtection().inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println(JsonHelper.toJson(event));
            }
        }).forPath("/nodecache/child1"));

        Thread.sleep(10000);
    }

    @Test
    public void testCreate3() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.create().withProtection().forPath("/nodecache/child1"));

        Thread.sleep(10000);
    }

    @Test
    public void testCreate4() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.create().forPath("/nodecache/child1"));

        Thread.sleep(10000);
    }

    @Test
    public void testCreate5() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/nodecache/child1", "123".getBytes()));

        Thread.sleep(10000);
    }

    @Test
    public void testGetChildren() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(client.getChildren().inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println(JsonHelper.toJson(event));
            }
        }).forPath("/nodecache"));

        Thread.sleep(10000);
    }

    @Test
    public void testGetChildren1() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        System.out.println(JsonHelper.toJson(client.getChildren().forPath("/nodecache")));

        Thread.sleep(10000);
    }

    @Test
    public void testDelete() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000, 4));
        client.start();
        client.delete().inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println(JsonHelper.toJson(curatorEvent));
            }
        }).forPath("/curd-test/delete");

        Thread.sleep(10000);
    }
}
