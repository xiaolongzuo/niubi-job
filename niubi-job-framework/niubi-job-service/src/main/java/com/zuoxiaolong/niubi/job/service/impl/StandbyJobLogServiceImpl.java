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

import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobLog;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobSummary;
import com.zuoxiaolong.niubi.job.service.StandbyJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class StandbyJobLogServiceImpl extends AbstractService implements StandbyJobLogService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public List<StandbyJobLog> getAllJobLogs() {
        return baseDao.getAll(StandbyJobLog.class);
    }

    @Override
    public String saveJobLog(StandbyJobSummary standbyJobSummary) {
        StandbyJobLog standbyJobLog = new StandbyJobLog();
        ReflectHelper.copyFieldValues(standbyJobSummary, standbyJobLog);
        return baseDao.save(standbyJobLog);
    }

    @Override
    public void updateJobLog(StandbyJobData.Data data) {
        if (StringHelper.isEmpty(data.getJobOperationLogId())) {
            return;
        }
        StandbyJobLog standbyJobLog = baseDao.get(StandbyJobLog.class, data.getJobOperationLogId());
        if (standbyJobLog == null) {
            return;
        }
        int retryTimes = 10;
        ReflectHelper.copyFieldValuesSkipNull(data, standbyJobLog);
        //retry, because the update operation may occur before the save operation.
        while (retryTimes-- > 0) {
            try {
                baseDao.update(standbyJobLog);
            } catch (IllegalArgumentException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    //ignored
                }
            }
        }
        data.clearOperationLog();
        standbyApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
    }

}
