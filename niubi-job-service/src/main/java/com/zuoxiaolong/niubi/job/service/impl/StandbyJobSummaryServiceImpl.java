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

package com.zuoxiaolong.niubi.job.service.impl;

import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJob;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobSummary;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import com.zuoxiaolong.niubi.job.service.StandbyJobLogService;
import com.zuoxiaolong.niubi.job.service.StandbyJobService;
import com.zuoxiaolong.niubi.job.service.StandbyJobSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class StandbyJobSummaryServiceImpl extends AbstractService implements StandbyJobSummaryService {

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private StandbyJobService standbyJobService;

    @Autowired
    private StandbyJobLogService standbyJobLogService;

    @Override
    public List<StandbyJobSummary> getAllJobSummaries() {
        return baseDao.getAll(StandbyJobSummary.class);
    }

    @Override
    public synchronized void saveJobSummary(StandbyJobSummary standbyJobSummary) {
        StandbyJobData standbyJobData = standbyApiFactory.jobApi().getJob(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName());
        StandbyJobData.Data data;
        if (standbyJobData == null) {
            data = new StandbyJobData.Data();
        } else {
            data = standbyJobData.getData();
        }
        //set data
        ReflectHelper.copyFieldValuesSkipNull(standbyJobSummary, data);
        StandbyJob standbyJob = standbyJobService.getJob(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName(), standbyJobSummary.getJarFileName());
        data.setJobOperationLogId(standbyJobLogService.saveJobLog(standbyJobSummary));
        data.setPackagesToScan(standbyJob.getPackagesToScan());
        data.setContainerType(standbyJob.getContainerType());
        //set state to Executing
        StandbyJobSummary param = new StandbyJobSummary();
        param.setGroupName(data.getGroupName());
        param.setJobName(data.getJobName());
        StandbyJobSummary standbyJobSummaryInDb = baseDao.getUnique(StandbyJobSummary.class, param);
        standbyJobSummaryInDb.setJobState("Executing");
        baseDao.update(standbyJobSummaryInDb);
        //send job
        standbyApiFactory.jobApi().saveJob(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName(), data);
    }

    @Override
    public void updateJobSummary(StandbyJobData.Data data) {
        StandbyJobSummary param = new StandbyJobSummary();
        param.setGroupName(data.getGroupName());
        param.setJobName(data.getJobName());
        StandbyJobSummary standbyJobSummary = baseDao.getUnique(StandbyJobSummary.class, param);
        ReflectHelper.copyFieldValuesSkipNull(data, standbyJobSummary);
        baseDao.update(standbyJobSummary);
    }

    @Override
    public StandbyJobSummary getJobSummary(String id) {
        StandbyJobSummary standbyJobSummary = baseDao.get(StandbyJobSummary.class, id);
        StandbyJobData standbyJobData = standbyApiFactory.jobApi().getJob(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName());
        if (standbyJobData != null) {
            ReflectHelper.copyFieldValues(standbyJobData.getData(), standbyJobSummary);
            standbyJobSummary.setOriginalJarFileName(standbyJobData.getData().getJarFileName());
        } else {
            List<String> jarFileNameList = standbyJobService.getJarFileNameList(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName());
            if (ListHelper.isEmpty(jarFileNameList)) {
                throw new ServiceException("job detail not found.");
            } else {
                standbyJobSummary.setOriginalJarFileName(jarFileNameList.get(0));
            }
        }
        return standbyJobSummary;
    }

    @Override
    public void updateJobSummary(String id) {
        StandbyJobSummary standbyJobSummary = baseDao.get(StandbyJobSummary.class, id);
        StandbyJobData standbyJobData = standbyApiFactory.jobApi().getJob(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName());
        updateJobSummary(standbyJobData.getData());
    }

}
