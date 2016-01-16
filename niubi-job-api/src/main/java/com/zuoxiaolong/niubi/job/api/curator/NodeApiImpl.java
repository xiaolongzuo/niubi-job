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

import com.zuoxiaolong.niubi.job.api.NodeApi;
import com.zuoxiaolong.niubi.job.api.data.NodeData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 01:15
 */
public class NodeApiImpl extends AbstractCurdApiImpl implements NodeApi {

    public NodeApiImpl(CuratorFramework client) {
        super(client);
    }

    private String getNodeParentPath() {
        return getPathApi().getStandbyNodePath().substring(0, getPathApi().getStandbyNodePath().lastIndexOf("/"));
    }

    @Override
    public List<NodeData> selectAllStandbyNodes() {
        List<ChildData> childDataList = selectChildDataList(getNodeParentPath());
        List<NodeData> nodeModelList = childDataList.stream().map(NodeData::new).collect(Collectors.toList());
        return nodeModelList;
    }

    @Override
    public String createStandbyNode(NodeData.Data data) {
        NodeData nodeData = new NodeData(getPathApi().getStandbyNodePath(), data);
        return insertEphemeralSequential(nodeData.getPath(), nodeData.getDataBytes());
    }

    @Override
    public void updateStandbyNode(String path, NodeData.Data data) {
        NodeData nodeData = new NodeData(path, data);
        update(nodeData.getPath(), nodeData.getDataBytes());
    }

    @Override
    public NodeData selectStandbyNode(String path) {
        return new NodeData(selectChildData(path));
    }


}
