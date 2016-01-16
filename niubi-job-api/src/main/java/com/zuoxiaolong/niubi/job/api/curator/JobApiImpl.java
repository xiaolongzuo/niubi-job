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

import com.zuoxiaolong.niubi.job.api.JobApi;
import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 01:15
 */
public class JobApiImpl extends AbstractCurdApiImpl implements JobApi {

    public JobApiImpl(CuratorFramework client) {
        super(client);
    }

    @Override
    public List<JobData> selectAllStandbyJobs() {
        List<ChildData> childDataList = selectChildDataList(getPathApi().getStandbyJobPath());
        List<JobData> nodeModelList = childDataList.stream().map(JobData::new).collect(Collectors.toList());
        return nodeModelList;
    }

    @Override
    public void createStandbyJob(JobData jobData) {
        insert(jobData.getPath(), JsonHelper.toBytes(jobData.getData()));
    }

    @Override
    public void updateStandbyJob(JobData jobData) {
        update(jobData.getPath(), jobData.getDataBytes());
    }

}
