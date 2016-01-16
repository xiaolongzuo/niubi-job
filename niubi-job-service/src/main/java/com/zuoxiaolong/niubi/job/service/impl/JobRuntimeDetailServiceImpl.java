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

import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.JobRuntimeDetail;
import com.zuoxiaolong.niubi.job.service.JobDetailService;
import com.zuoxiaolong.niubi.job.service.JobOperationLogService;
import com.zuoxiaolong.niubi.job.service.JobRuntimeDetailService;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/17 00:10
 */
@Service
public class JobRuntimeDetailServiceImpl extends AbstractService implements JobRuntimeDetailService {

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private JobDetailService jobDetailService;

    @Autowired
    private JobOperationLogService jobOperationLogService;

    @Override
    public List<JobRuntimeDetail> getAllStandbyJobRuntimeDetails() {
        return baseDao.getAll(JobRuntimeDetail.class);
    }

    @Override
    public void createStandbyJob(JobRuntimeDetail jobRuntimeDetail) {
        JobData jobData = apiFactory.jobApi().selectStandbyJob(jobRuntimeDetail.getGroupName(), jobRuntimeDetail.getJobName());
        JobData.Data data;
        if (jobData == null) {
            data = new JobData.Data();
        } else {
            data = jobData.getData();
        }
        ReflectHelper.copyFieldValuesSkipNull(jobRuntimeDetail, data);
        data.setJobOperationLogId(jobOperationLogService.save(jobRuntimeDetail));
        apiFactory.jobApi().createStandbyJob(jobRuntimeDetail.getGroupName(), jobRuntimeDetail.getJobName(), data);
    }

    @Override
    public void update(JobData.Data data) {
        JobRuntimeDetail param = new JobRuntimeDetail();
        param.setGroupName(data.getGroupName());
        param.setJobName(data.getJobName());
        JobRuntimeDetail jobRuntimeDetail = baseDao.getUnique(JobRuntimeDetail.class, param);
        ReflectHelper.copyFieldValuesSkipNull(data, jobRuntimeDetail);
        baseDao.update(jobRuntimeDetail);
        jobOperationLogService.update(data);
    }

    @Override
    public JobRuntimeDetail getStandbyJobRuntimeDetail(String id) {
        JobRuntimeDetail jobRuntimeDetail = baseDao.get(JobRuntimeDetail.class, id);
        JobData jobData = apiFactory.jobApi().selectStandbyJob(jobRuntimeDetail.getGroupName(), jobRuntimeDetail.getJobName());
        if (jobData != null) {
            ReflectHelper.copyFieldValues(jobData.getData(), jobRuntimeDetail);
            jobRuntimeDetail.setOriginalJarFileName(jobData.getData().getJarFileName());
        } else {
            List<String> jarFileNameList = jobDetailService.getJarFileNameList(jobRuntimeDetail.getGroupName(), jobRuntimeDetail.getJobName());
            if (ListHelper.isEmpty(jarFileNameList)) {
                throw new ServiceException("job detail not found.");
            } else {
                jobRuntimeDetail.setOriginalJarFileName(jarFileNameList.get(0));
            }
        }
        return jobRuntimeDetail;
    }

}
