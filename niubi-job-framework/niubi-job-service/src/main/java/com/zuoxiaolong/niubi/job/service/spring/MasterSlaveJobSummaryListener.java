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
import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobLogService;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobSummaryService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 该监听器用于启动一个cache去监听主从集群任务节点数据的变化,并且同步到console数据库
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component
public class MasterSlaveJobSummaryListener {

    @Autowired
    private MasterSlaveJobSummaryService masterSlaveJobSummaryService;

    @Autowired
    private MasterSlaveJobLogService masterSlaveJobLogService;

    @Autowired
    private CuratorFramework client;

    @PostConstruct
    public void listen() throws Exception {
        MasterSlaveApiFactory masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, masterSlaveApiFactory.pathApi().getJobPath(), true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework clientInner, PathChildrenCacheEvent event) throws Exception {
                if (!EventHelper.isChildUpdateEvent(event) && !EventHelper.isChildAddEvent(event)) {
                    return;
                }
                MasterSlaveJobData masterSlaveJobData = new MasterSlaveJobData(event.getData());
                if (!masterSlaveJobData.getData().isOperated()) {
                    return;
                }
                LoggerHelper.info("begin update master-slave job summary " + masterSlaveJobData.getData());
                masterSlaveJobSummaryService.updateJobSummary(masterSlaveJobData.getData());
                masterSlaveJobLogService.updateJobLog(masterSlaveJobData.getData());
                LoggerHelper.info("update master-slave job summary successfully " + masterSlaveJobData.getData());
            }
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

}
