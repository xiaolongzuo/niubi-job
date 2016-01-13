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

import com.google.gson.Gson;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Xiaolong Zuo
 * @since 16/1/10 18:56
 */
public class CacheTest {

    @Test
    public void testTreeCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000,4));
        client.start();

        final TreeCache nodeCache = new TreeCache(client , "/");
        nodeCache.getListenable().addListener(new TreeCacheListener() {
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                System.out.println(new Gson().toJson(event));
            }
        });
        nodeCache.start();

        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    @Test
    public void testNodeCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000,4));
        client.start();

        final NodeCache nodeCache = new NodeCache(client , "/nodecache/xiayiji");
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                if (nodeCache.getCurrentData() == null) {
                    System.out.println("节点被删除了");
                } else {
                    System.out.println(nodeCache.getCurrentData().getPath());
                    System.out.println(nodeCache.getCurrentData().getStat());
                    System.out.println(new String(nodeCache.getCurrentData().getData()));
                    System.out.println("------------------------");
                }
            }
        });
        nodeCache.start(true);

        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    @Test
    public void testChildrenCache() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181,localhost:3181,localhost:4181", new ExponentialBackoffRetry(1000,4));
        client.start();

        final PathChildrenCache nodeCache = new PathChildrenCache(client , "/nodecache", true);
        nodeCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println(new Gson().toJson(event));
                System.out.println(new Gson().toJson(nodeCache.getCurrentData()));
                System.out.println(new Gson().toJson(nodeCache.getCurrentData("/nodecache/child1")));
                System.out.println("------------------------");
            }
        });
        nodeCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
}
