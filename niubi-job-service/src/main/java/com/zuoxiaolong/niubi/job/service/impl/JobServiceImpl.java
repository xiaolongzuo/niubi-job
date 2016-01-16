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
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.Job;
import com.zuoxiaolong.niubi.job.persistent.entity.JobJar;
import com.zuoxiaolong.niubi.job.service.JobJarService;
import com.zuoxiaolong.niubi.job.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobServiceImpl extends AbstractService implements JobService {

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private JobJarService jobJarService;

    @Override
    public List<Job> getAllStandbyJobs() {
        return baseDao.getAll(Job.class);
    }

    @Override
    public Job getJob(String id) {
        return baseDao.get(Job.class, id);
    }

    @Override
    public List<Job> getSameGroupAndNameJobs(String group, String name) {
        Job job = new Job();
        job.setGroupName(group);
        job.setJobName(name);
        return baseDao.getList(Job.class, job, false);
    }

    @Override
    public void update(Job job) {
        JobJar jobJar = jobJarService.getJobJar(job.getJarFileName());
        //set properties
        String path = apiFactory.pathApi().getStandbyJobPath() + "/" + job.getGroupName() + "." +job.getJobName();
        JobData.Data data = new JobData.Data();
        data.setGroup(job.getGroupName());
        data.setName(job.getJobName());
        data.setMode(job.getMode());
        data.setJarFileName(job.getJarFileName());
        data.setPackagesToScan(jobJar.getPackagesToScan());
        data.setCron(job.getCron());
        data.setMisfirePolicy(job.getMisfirePolicy());
        data.setOperation(job.getOperation());
        if ("Start".equals(job.getOperation()) || "Restart".equals(job.getOperation())) {
            job.setState("STARTUP");
            data.setState("STARTUP");
        } else if ("Pause".equals(job.getOperation())) {
            job.setState("Pause");
            data.setState("Pause");
        } else {
            LoggerHelper.warn("invalid operation [" + job.getOperation() + "]");
            return;
        }

        JobData jobData = new JobData(path, data);
        apiFactory.jobApi().createStandbyJob(jobData);
        job.setJobJar(jobJar);
        baseDao.update(job);
    }

}
