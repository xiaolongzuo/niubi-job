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

import com.zuoxiaolong.niubi.job.api.ApiFactory;
import com.zuoxiaolong.niubi.job.api.JobJarApi;
import com.zuoxiaolong.niubi.job.api.PathApi;
import com.zuoxiaolong.niubi.job.api.model.JobJarModel;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 01:15
 */
public class JobJarApiImpl implements JobJarApi {

    private CuratorFramework client;

    private PathApi pathApi;

    public JobJarApiImpl(CuratorFramework client) {
        this.client = client;
        this.pathApi = ApiFactory.instance().pathApi();
    }

    @Override
    public List<JobJarModel> getStandbyNodeJobJarList() throws Exception {
        List<JobJarModel> jobJarModelList = new ArrayList<>();
        List<String> children = client.getChildren().forPath(pathApi.getStandbyNodeJobJarPath());
        for (String jarFileName : children) {
            String path = pathApi.getStandbyNodeJobJarPath() + "/" + jarFileName;
            try {
                byte[] bytes = client.getData().forPath(path);
                jobJarModelList.add(new JobJarModel(path, bytes));
            } catch (Exception e) {
                LoggerHelper.error("get data failed [" + path + "]", e);
            }
        }
        return jobJarModelList;
    }

}
