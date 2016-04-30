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

import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJob;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobLogService;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobService;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobSummaryService;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class MasterSlaveJobSummaryServiceImpl extends AbstractService implements MasterSlaveJobSummaryService {

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private MasterSlaveJobService masterSlaveJobService;

    @Autowired
    private MasterSlaveJobLogService masterSlaveJobLogService;

    @Override
    public List<MasterSlaveJobSummary> getAllJobSummaries() {
        return baseDao.getAll(MasterSlaveJobSummary.class);
    }

    @Override
    public synchronized void saveJobSummary(MasterSlaveJobSummary masterSlaveJobSummary) {
        MasterSlaveJobData masterSlaveJobData = masterSlaveApiFactory.jobApi().getJob(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName());
        MasterSlaveJobData.Data data;
        if (masterSlaveJobData == null) {
            data = new MasterSlaveJobData.Data();
        } else {
            data = masterSlaveJobData.getData();
        }
        //set data
        ReflectHelper.copyFieldValuesSkipNull(masterSlaveJobSummary, data);
        MasterSlaveJob masterSlaveJob = masterSlaveJobService.getJob(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName(), masterSlaveJobSummary.getJarFileName());
        data.setJobOperationLogId(masterSlaveJobLogService.saveJobLog(masterSlaveJobSummary));
        data.setPackagesToScan(masterSlaveJob.getPackagesToScan());
        data.setContainerType(masterSlaveJob.getContainerType());
        //set state to Executing
        MasterSlaveJobSummary param = new MasterSlaveJobSummary();
        param.setGroupName(data.getGroupName());
        param.setJobName(data.getJobName());
        MasterSlaveJobSummary masterSlaveJobSummaryInDb = baseDao.getUnique(MasterSlaveJobSummary.class, param);
        masterSlaveJobSummaryInDb.setJobState("Executing");
        baseDao.update(masterSlaveJobSummaryInDb);
        //send job
        masterSlaveApiFactory.jobApi().saveJob(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName(), data);
    }

    @Override
    public void updateJobSummary(MasterSlaveJobData.Data data) {
        MasterSlaveJobSummary param = new MasterSlaveJobSummary();
        param.setGroupName(data.getGroupName());
        param.setJobName(data.getJobName());
        MasterSlaveJobSummary masterSlaveJobSummary = baseDao.getUnique(MasterSlaveJobSummary.class, param);
        ReflectHelper.copyFieldValuesSkipNull(data, masterSlaveJobSummary);
        baseDao.update(masterSlaveJobSummary);
    }

    @Override
    public void updateJobSummary(String id) {
        MasterSlaveJobSummary masterSlaveJobSummary = baseDao.get(MasterSlaveJobSummary.class, id);
        MasterSlaveJobData masterSlaveJobData = masterSlaveApiFactory.jobApi().getJob(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName());
        updateJobSummary(masterSlaveJobData.getData());
    }

    @Override
    public MasterSlaveJobSummary getJobSummary(String id) {
        MasterSlaveJobSummary masterSlaveJobSummary = baseDao.get(MasterSlaveJobSummary.class, id);
        MasterSlaveJobData masterSlaveJobData = masterSlaveApiFactory.jobApi().getJob(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName());
        if (masterSlaveJobData != null) {
            ReflectHelper.copyFieldValues(masterSlaveJobData.getData(), masterSlaveJobSummary);
            masterSlaveJobSummary.setOriginalJarFileName(masterSlaveJobData.getData().getJarFileName());
        } else {
            List<String> jarFileNameList = masterSlaveJobService.getJarFileNameList(masterSlaveJobSummary.getGroupName(), masterSlaveJobSummary.getJobName());
            if (ListHelper.isEmpty(jarFileNameList)) {
                throw new ServiceException("job detail not found.");
            } else {
                masterSlaveJobSummary.setOriginalJarFileName(jarFileNameList.get(0));
            }
        }
        return masterSlaveJobSummary;
    }

}
