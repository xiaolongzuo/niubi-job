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

import com.zuoxiaolong.niubi.job.api.StandbyNodeApi;
import com.zuoxiaolong.niubi.job.api.data.StandbyNodeData;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StandbyNodeApiImpl extends AbstractCurdApiImpl implements StandbyNodeApi {

    public StandbyNodeApiImpl(CuratorFramework client) {
        super(client);
    }

    @Override
    public List<StandbyNodeData> getAllNodes() {
        List<ChildData> childDataList = getChildren(PathHelper.getParentPath(getStandbyPathApi().getNodePath()));
        return childDataList.stream().map(StandbyNodeData::new).collect(Collectors.toList());
    }

    @Override
    public String saveNode(StandbyNodeData.Data data) {
        StandbyNodeData standbyNodeData = new StandbyNodeData(getStandbyPathApi().getNodePath(), data);
        return createEphemeralSequential(standbyNodeData.getPath(), standbyNodeData.getDataBytes());
    }

    @Override
    public void updateNode(String path, StandbyNodeData.Data data) {
        StandbyNodeData standbyNodeData = new StandbyNodeData(path, data);
        setData(standbyNodeData.getPath(), standbyNodeData.getDataBytes());
    }

    @Override
    public StandbyNodeData getNode(String path) {
        return new StandbyNodeData(getData(path));
    }


    @Override
    public void deleteNode(String path) {
        delete(path);
    }

}
