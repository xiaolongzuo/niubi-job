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

import com.zuoxiaolong.niubi.job.api.MasterSlaveApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.MasterSlaveApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobLog;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobLogService;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobService;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobSummaryService;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class MasterSlaveJobLogServiceImplTest extends AbstractSpringContextTest{

    @Autowired
    private MasterSlaveJobLogService masterSlaveJobLogService;

    @Autowired
    private MasterSlaveJobSummaryService masterSlaveJobSummaryService;

    @Autowired
    private MasterSlaveJobService masterSlaveJobService;

    @Autowired
    private CuratorFramework client;

    @Autowired
    private BaseDao baseDao;

    @Test
    public void getAllJobLogs() {
        List<MasterSlaveJobLog> jobLogs = masterSlaveJobLogService.getAllJobLogs();
        Assert.assertNotNull(jobLogs);
        Assert.assertTrue(jobLogs.size() == 0);
        MasterSlaveJobSummary jobSummary = new MasterSlaveJobSummary();
        jobSummary.setJarFileName("1.jar");
        jobSummary.setJobName("Job1");
        jobSummary.setPackagesToScan("com.zuoxiaolong");
        jobSummary.setGroupName("com.zuoxiaolong.job");
        jobSummary.setContainerType("Common");
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setJobOperation("Start");
        jobSummary.setJobState("Startup");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setOriginalJarFileName("1.jar");
        masterSlaveJobLogService.saveJobLog(jobSummary);
        jobLogs = masterSlaveJobLogService.getAllJobLogs();
        Assert.assertNotNull(jobLogs);
        Assert.assertTrue(jobLogs.size() == 1);
        MasterSlaveJobLog jobLog = jobLogs.get(0);
        Assert.assertEquals(jobLog.getJarFileName(), "1.jar");
        Assert.assertEquals(jobLog.getJobName(), "Job1");
        Assert.assertEquals(jobLog.getJobOperation(), "Start");
        Assert.assertEquals(jobLog.getMisfirePolicy(), "None");
        Assert.assertEquals(jobLog.getOriginalJarFileName(), "1.jar");
        Assert.assertEquals(jobLog.getJobCron(), "0 * * * * ?");
        Assert.assertEquals(jobLog.getContainerType(), "Common");
        Assert.assertEquals(jobLog.getGroupName(), "com.zuoxiaolong.job");
    }

    @Test
    public void updateJobLog() {
        //上传Jar包
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<MasterSlaveJobSummary> jobSummaries = masterSlaveJobSummaryService.getAllJobSummaries();
        //调度任务1
        MasterSlaveJobSummary jobSummary = jobSummaries.get(0);
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setOriginalJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setJobOperation("Start");
        jobSummary.setContainerType("Spring");
        masterSlaveJobSummaryService.saveJobSummary(jobSummary);
        String jobLogId = masterSlaveJobLogService.saveJobLog(jobSummary);
        //根据节点数据更新日志
        MasterSlaveApiFactory apiFactory = new MasterSlaveApiFactoryImpl(client);
        MasterSlaveJobData.Data data = apiFactory.jobApi().getJob(jobSummary.getGroupName(), jobSummary.getJobName()).getData();
        masterSlaveJobLogService.updateJobLog(data);
        //判断日志是否更新成功
        MasterSlaveJobLog jobLog = baseDao.get(MasterSlaveJobLog.class, jobLogId);
        Assert.assertEquals(jobLog.getJarFileName(), "niubi-job-sample-spring.jar");
        Assert.assertEquals(jobLog.getJobName(), "test");
        Assert.assertEquals(jobLog.getJobOperation(), "Start");
        Assert.assertEquals(jobLog.getMisfirePolicy(), "None");
        Assert.assertEquals(jobLog.getOriginalJarFileName(), "niubi-job-sample-spring.jar");
        Assert.assertEquals(jobLog.getJobCron(), "0 * * * * ?");
        Assert.assertEquals(jobLog.getContainerType(), "Spring");
        Assert.assertEquals(jobLog.getGroupName(), "com.zuoxiaolong.niubi.job.sample.spring.job.Job1");
    }

}
