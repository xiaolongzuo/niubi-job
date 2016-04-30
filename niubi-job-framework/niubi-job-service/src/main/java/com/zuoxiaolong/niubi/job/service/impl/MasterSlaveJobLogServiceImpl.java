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

import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobLog;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class MasterSlaveJobLogServiceImpl extends AbstractService implements MasterSlaveJobLogService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public List<MasterSlaveJobLog> getAllJobLogs() {
        return baseDao.getAll(MasterSlaveJobLog.class);
    }

    @Override
    public String saveJobLog(MasterSlaveJobSummary masterSlaveJobSummary) {
        MasterSlaveJobLog masterSlaveJobLog = new MasterSlaveJobLog();
        ReflectHelper.copyFieldValues(masterSlaveJobSummary, masterSlaveJobLog);
        return baseDao.save(masterSlaveJobLog);
    }

    @Override
    public void updateJobLog(MasterSlaveJobData.Data data) {
        if (StringHelper.isEmpty(data.getJobOperationLogId())) {
            return;
        }
        MasterSlaveJobLog masterSlaveJobLog = baseDao.get(MasterSlaveJobLog.class, data.getJobOperationLogId());
        if (masterSlaveJobLog == null) {
            return;
        }
        int retryTimes = 10;
        ReflectHelper.copyFieldValuesSkipNull(data, masterSlaveJobLog);
        //retry, because the update operation may occur before the save operation.
        while (retryTimes-- > 0) {
            try {
                baseDao.update(masterSlaveJobLog);
            } catch (IllegalArgumentException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    //ignored
                }
            }
        }
        data.clearOperationLog();
        masterSlaveApiFactory.jobApi().updateJob(data.getGroupName(), data.getJobName(), data);
    }

}
