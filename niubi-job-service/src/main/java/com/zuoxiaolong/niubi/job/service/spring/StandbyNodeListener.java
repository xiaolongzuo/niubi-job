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

package com.zuoxiaolong.niubi.job.service.spring;

import com.zuoxiaolong.niubi.job.api.MasterSlaveApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.MasterSlaveApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.StandbyNodeData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.service.StandbyNodeService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Xiaolong Zuo
 * @since 16/1/17 03:55
 */
@Component
public class StandbyNodeListener {

    @Autowired
    private StandbyNodeService standbyNodeService;

    @Autowired
    private CuratorFramework client;

    public void listen() throws Exception {
        MasterSlaveApiFactory masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);
        String nodeParentPath = PathHelper.getParentPath(masterSlaveApiFactory.pathApi().getNodePath());
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, nodeParentPath, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework clientInner, PathChildrenCacheEvent event) throws Exception {
                if (!EventHelper.isChildModifyEvent(event)) {
                    return;
                }
                StandbyNodeData standbyNodeData = new StandbyNodeData(event.getData());
                LoggerHelper.info("begin update master-slave node data " + standbyNodeData.getData());
                standbyNodeService.saveNode(standbyNodeData);
                LoggerHelper.info("update master-slave node data successfully " + standbyNodeData.getData());
            }
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

}
