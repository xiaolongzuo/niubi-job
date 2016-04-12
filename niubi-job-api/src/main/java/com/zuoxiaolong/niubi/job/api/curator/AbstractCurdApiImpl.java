/*
 * Copyright 2002-2015 the original author or authors.
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

import com.zuoxiaolong.niubi.job.api.MasterSlavePathApi;
import com.zuoxiaolong.niubi.job.api.StandbyPathApi;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象的ZK CURD操作API实现类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractCurdApiImpl {

    private static final Stat EMPTY_STAT = new Stat();

    private CuratorFramework client;

    private StandbyPathApi standbyPathApi = StandbyPathApiImpl.INSTANCE;

    private MasterSlavePathApi masterSlavePathApi = MasterSlavePathApiImpl.INSTANCE;

    public AbstractCurdApiImpl(CuratorFramework client) {
        this.client = client;
    }

    /**
     * 子类可以通过该方法获取curator客户端
     *
     * @return curator客户端
     */
    protected CuratorFramework getClient() {
        return client;
    }

    /**
     * 获取主备模式下的PATH API
     *
     * @return 主备模式下的PATH API
     */
    protected StandbyPathApi getStandbyPathApi() {
        return standbyPathApi;
    }

    /**
     * 获取主从模式下的PATH API
     *
     * @return 主从模式下的PATH API
     */
    protected MasterSlavePathApi getMasterSlavePathApi() {
        return masterSlavePathApi;
    }

    /**
     * 获取该path下的所有子节点
     *
     * @param path 父节点路径
     * @return 所有子节点
     */
    protected List<ChildData> getChildren(String path) {
        if (!checkExists(path)) {
            return new ArrayList<>();
        }
        try {
            List<ChildData> childDataList = new ArrayList<>();
            List<String> children = client.getChildren().forPath(path);
            childDataList.addAll(children.stream().map(child -> getData(path + "/" + child)).collect(Collectors.toList()));
            return childDataList;
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    /**
     * 获取该节点的数据
     *
     * @param path 节点路径
     * @return 节点数据
     */
    protected ChildData getData(String path) {
        try {
            return new ChildData(path, EMPTY_STAT, client.getData().forPath(path));
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    /**
     * 检查该节点是否存在
     *
     * @param path 节点路径
     * @return 如果存在返回true,否则为false
     */
    protected boolean checkExists(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    /**
     * 创建一个节点
     *
     * @param path 节点路径
     * @param data 节点数据
     * @return 节点路径
     */
    protected String create(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String createWithProtection(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().withProtection().forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String createPersistentWithProtection(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String createPersistentSequentialWithProtection(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String createPersistentSequential(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String createEphemeralSequential(String path, byte[] data) {
        try {
            return getClient().create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected void delete(String path) {
        try {
            getClient().delete().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected Stat setData(String path, byte[] data) {
        try {
            return getClient().setData().forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

}
