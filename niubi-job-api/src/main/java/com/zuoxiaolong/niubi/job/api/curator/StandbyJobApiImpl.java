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

import com.zuoxiaolong.niubi.job.api.StandbyJobApi;
import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StandbyJobApiImpl extends AbstractCurdApiImpl implements StandbyJobApi {

    public StandbyJobApiImpl(CuratorFramework client) {
        super(client);
    }

    @Override
    public List<StandbyJobData> getAllJobs() {
        List<ChildData> childDataList = getChildren(getStandbyPathApi().getJobPath());
        return childDataList.stream().map(StandbyJobData::new).collect(Collectors.toList());
    }

    @Override
    public void saveJob(String group, String name, StandbyJobData.Data data) {
        data.prepareOperation();
        StandbyJobData standbyJobData = new StandbyJobData(PathHelper.getJobPath(getStandbyPathApi().getJobPath(), group, name), data);
        standbyJobData.getData().incrementVersion();
        if (checkExists(standbyJobData.getPath())) {
            setData(standbyJobData.getPath(), standbyJobData.getDataBytes());
        } else {
            create(standbyJobData.getPath(), JsonHelper.toBytes(standbyJobData.getData()));
        }
    }

    @Override
    public void updateJob(String group, String name, StandbyJobData.Data data) {
        StandbyJobData standbyJobData = new StandbyJobData(PathHelper.getJobPath(getStandbyPathApi().getJobPath(), group, name), data);
        standbyJobData.getData().incrementVersion();
        setData(standbyJobData.getPath(), standbyJobData.getDataBytes());
    }

    @Override
    public StandbyJobData getJob(String group, String name) {
        return getJob(PathHelper.getJobPath(getStandbyPathApi().getJobPath(), group, name));
    }

    @Override
    public StandbyJobData getJob(String path) {
        if (!checkExists(path)) {
            return null;
        }
        return new StandbyJobData(getData(path));
    }

}
