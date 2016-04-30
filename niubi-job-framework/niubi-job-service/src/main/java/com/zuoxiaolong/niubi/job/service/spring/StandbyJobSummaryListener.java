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

import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.StandbyApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.service.StandbyJobLogService;
import com.zuoxiaolong.niubi.job.service.StandbyJobSummaryService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 该监听器用于启动一个cache去监听主备集群任务节点数据的变化,并且同步到console数据库
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component
public class StandbyJobSummaryListener {

    @Autowired
    private StandbyJobSummaryService standbyJobSummaryService;

    @Autowired
    private StandbyJobLogService standbyJobLogService;

    @Autowired
    private CuratorFramework client;

    @PostConstruct
    public void listen() throws Exception {
        StandbyApiFactory standbyApiFactory = new StandbyApiFactoryImpl(client);
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, standbyApiFactory.pathApi().getJobPath(), true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public synchronized void childEvent(CuratorFramework clientInner, PathChildrenCacheEvent event) throws Exception {
                if (!EventHelper.isChildUpdateEvent(event) && !EventHelper.isChildAddEvent(event)) {
                    return;
                }
                StandbyJobData standbyJobData = new StandbyJobData(event.getData());
                if (!standbyJobData.getData().isOperated()) {
                    return;
                }
                LoggerHelper.info("begin update standby job summary " + standbyJobData.getData());
                standbyJobSummaryService.updateJobSummary(standbyJobData.getData());
                standbyJobLogService.updateJobLog(standbyJobData.getData());
                LoggerHelper.info("update standby job summary successfully " + standbyJobData.getData());
            }
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

}
