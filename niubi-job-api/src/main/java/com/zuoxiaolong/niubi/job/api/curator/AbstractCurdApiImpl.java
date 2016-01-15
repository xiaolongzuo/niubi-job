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

import com.zuoxiaolong.niubi.job.api.PathApi;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:08
 */
public abstract class AbstractCurdApiImpl {

    private static final Stat EMPTY_STAT = new Stat();

    private CuratorFramework client;

    private PathApi pathApi = PathApiImpl.INSTANCE;

    public AbstractCurdApiImpl(CuratorFramework client) {
        this.client = client;
    }

    protected CuratorFramework getClient() {
        return client;
    }

    protected PathApi getPathApi() {
        return pathApi;
    }

    protected List<ChildData> selectChildDataList(String path) {
        try {
            List<ChildData> childDataList = new ArrayList<>();
            List<String> children = client.getChildren().forPath(path);
            childDataList.addAll(children.stream().map(child -> selectChildData(path + "/" + child)).collect(Collectors.toList()));
            return childDataList;
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected ChildData selectChildData(String path) {
        try {
            ChildData childData = new ChildData(path, EMPTY_STAT, client.getData().forPath(path));
            return childData;
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected String insert(String path, byte[] data) {
        try {
            return getClient().create().forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected void delete(String path) {
        try {
            getClient().delete().forPath(path);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

    protected Stat update(String path, byte[] data) {
        try {
            return getClient().setData().forPath(path, data);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

}
