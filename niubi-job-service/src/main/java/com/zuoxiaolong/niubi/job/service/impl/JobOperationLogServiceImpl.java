/*
 * Copyright 2002-2015 the original author or authors.
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

import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.JobOperationLog;
import com.zuoxiaolong.niubi.job.persistent.entity.JobRuntimeDetail;
import com.zuoxiaolong.niubi.job.service.JobOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobOperationLogServiceImpl extends AbstractService implements JobOperationLogService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public List<JobOperationLog> getAllJobOperationLog() {
        return baseDao.getAll(JobOperationLog.class);
    }

    @Override
    public String save(JobRuntimeDetail jobRuntimeDetail) {
        JobOperationLog jobOperationLog = new JobOperationLog();
        ReflectHelper.copyFieldValues(jobRuntimeDetail, jobOperationLog);
        return baseDao.save(jobOperationLog);
    }

    @Override
    public void update(JobData.Data data) {
        if (!StringHelper.isEmpty(data.getJobOperationLogId())) {
            JobOperationLog jobOperationLog = baseDao.get(JobOperationLog.class, data.getJobOperationLogId());
            if (jobOperationLog != null) {
                int retryTimes = 10;
                ReflectHelper.copyFieldValuesSkipNull(data, jobOperationLog);
                //retry, because the update operation may occur before the save operation.
                while (retryTimes-- > 0) {
                    try {
                        baseDao.update(jobOperationLog);
                    } catch (IllegalArgumentException e) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            //ignored
                        }
                    }
                }

            }
            data.clearOperationLog();
            apiFactory.jobApi().updateStandbyJob(data.getGroupName(), data.getJobName(), data);
        }
    }

    @Override
    public JobOperationLog getJobOperationLog(String id) {
        return baseDao.get(JobOperationLog.class, id);
    }

}
