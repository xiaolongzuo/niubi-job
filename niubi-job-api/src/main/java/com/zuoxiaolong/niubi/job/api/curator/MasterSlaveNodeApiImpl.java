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

import com.zuoxiaolong.niubi.job.api.MasterSlaveNodeApi;
import com.zuoxiaolong.niubi.job.api.data.MasterSlaveNodeData;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class MasterSlaveNodeApiImpl extends AbstractCurdApiImpl implements MasterSlaveNodeApi {

    public MasterSlaveNodeApiImpl(CuratorFramework client) {
        super(client);
    }

    @Override
    public List<MasterSlaveNodeData> getAllNodes() {
        List<ChildData> childDataList = getChildren(PathHelper.getParentPath(getMasterSlavePathApi().getNodePath()));
        return childDataList.stream().map(MasterSlaveNodeData::new).collect(Collectors.toList());
    }

    @Override
    public String saveNode(MasterSlaveNodeData.Data data) {
        MasterSlaveNodeData masterSlaveNodeData = new MasterSlaveNodeData(getMasterSlavePathApi().getNodePath(), data);
        return createEphemeralSequential(masterSlaveNodeData.getPath(), masterSlaveNodeData.getDataBytes());
    }

    @Override
    public void updateNode(String path, MasterSlaveNodeData.Data data) {
        MasterSlaveNodeData masterSlaveNodeData = new MasterSlaveNodeData(path, data);
        setData(masterSlaveNodeData.getPath(), masterSlaveNodeData.getDataBytes());
    }

    @Override
    public MasterSlaveNodeData getNode(String path) {
        return new MasterSlaveNodeData(getData(path));
    }


    @Override
    public void deleteNode(String path) {
        delete(path);
    }

}
