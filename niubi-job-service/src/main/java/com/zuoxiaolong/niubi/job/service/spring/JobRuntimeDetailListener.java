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

import com.zuoxiaolong.niubi.job.api.ApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.ApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.api.helper.EventHelper;
import com.zuoxiaolong.niubi.job.service.JobRuntimeDetailService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Xiaolong Zuo
 * @since 16/1/17 03:55
 */
@Component
public class JobRuntimeDetailListener {

    @Autowired
    private JobRuntimeDetailService jobRuntimeDetailService;

    @Autowired
    private CuratorFramework client;

    public void listen() throws Exception {
        ApiFactory apiFactory = new ApiFactoryImpl(client);
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, apiFactory.pathApi().getStandbyJobPath(), true);
        pathChildrenCache.getListenable().addListener((clientInner, event) -> {
            if (!EventHelper.isChildUpdateEvent(event) && !EventHelper.isChildAddEvent(event)) {
                return;
            }
            JobData jobData = new JobData(event.getData());
            if (!jobData.getData().isOperated()) {
                return;
            }
            jobRuntimeDetailService.update(jobData.getData());
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

}
