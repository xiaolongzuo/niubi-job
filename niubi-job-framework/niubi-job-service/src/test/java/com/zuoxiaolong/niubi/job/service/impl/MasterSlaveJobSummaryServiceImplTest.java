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
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
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
public class MasterSlaveJobSummaryServiceImplTest extends AbstractSpringContextTest{

    @Autowired
    private MasterSlaveJobSummaryService masterSlaveJobSummaryService;

    @Autowired
    private MasterSlaveJobService masterSlaveJobService;

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private CuratorFramework client;

    @Test
    public void getAllJobSummaries() {
        List<MasterSlaveJobSummary> jobSummaries = masterSlaveJobSummaryService.getAllJobSummaries();
        Assert.assertNotNull(jobSummaries);
        Assert.assertTrue(jobSummaries.size() == 0);
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        jobSummaries = masterSlaveJobSummaryService.getAllJobSummaries();
        Assert.assertNotNull(jobSummaries);
        Assert.assertTrue(jobSummaries.size() == 2);
    }

    @Test
    public void saveJobSummary() {
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<MasterSlaveJobSummary> jobSummaries = masterSlaveJobSummaryService.getAllJobSummaries();
        MasterSlaveJobSummary jobSummary = jobSummaries.get(0);
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setOriginalJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setJobOperation("Start");
        jobSummary.setContainerType("Spring");
        masterSlaveJobSummaryService.saveJobSummary(jobSummary);
        jobSummary = baseDao.get(MasterSlaveJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("Executing", jobSummary.getJobState());
    }

    @Test
    public void getJobSummary() {
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<MasterSlaveJobSummary> jobSummaries = masterSlaveJobSummaryService.getAllJobSummaries();
        MasterSlaveJobSummary jobSummary = jobSummaries.get(0);
        String id = jobSummary.getId();
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setOriginalJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setJobOperation("Start");
        jobSummary.setContainerType("Spring");
        masterSlaveJobSummaryService.saveJobSummary(jobSummary);
        jobSummary = masterSlaveJobSummaryService.getJobSummary(id);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getJobState(), "Shutdown");
        MasterSlaveApiFactory apiFactory = new MasterSlaveApiFactoryImpl(client);
        MasterSlaveJobData.Data data = new MasterSlaveJobData.Data();
        data.setJobState("Startup");
        apiFactory.jobApi().updateJob(jobSummary.getGroupName(), jobSummary.getJobName(), data);
        jobSummary = masterSlaveJobSummaryService.getJobSummary(id);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getJobState(), "Startup");
    }

    @Test
    public void updateJobSummary1() {
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        MasterSlaveJobSummary jobSummary = masterSlaveJobSummaryService.getAllJobSummaries().get(0);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getGroupName(), "com.zuoxiaolong.niubi.job.sample.spring.job.Job1");
        Assert.assertEquals(jobSummary.getJobName(), "test");
        MasterSlaveJobData.Data data = new MasterSlaveJobData.Data();
        data.setJobCron("0 * * * * ?");
        data.setGroupName(jobSummary.getGroupName());
        data.setJobName(jobSummary.getJobName());
        masterSlaveJobSummaryService.updateJobSummary(data);
        jobSummary = baseDao.get(MasterSlaveJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("0 * * * * ?", jobSummary.getJobCron());
    }

    @Test
    public void updateJobSummary2() {
        masterSlaveJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        MasterSlaveJobSummary jobSummary = masterSlaveJobSummaryService.getAllJobSummaries().get(0);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getGroupName(), "com.zuoxiaolong.niubi.job.sample.spring.job.Job1");
        Assert.assertEquals(jobSummary.getJobName(), "test");
        masterSlaveJobSummaryService.saveJobSummary(jobSummary);
        MasterSlaveApiFactory apiFactory = new MasterSlaveApiFactoryImpl(client);
        MasterSlaveJobData.Data data = new MasterSlaveJobData.Data();
        data.setJobState("Startup");
        data.setJobCron("0 * * * * ?");
        data.setGroupName(jobSummary.getGroupName());
        data.setJobName(jobSummary.getJobName());
        apiFactory.jobApi().updateJob(jobSummary.getGroupName(), jobSummary.getJobName(), data);
        masterSlaveJobSummaryService.updateJobSummary(jobSummary.getId());
        jobSummary = baseDao.get(MasterSlaveJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("0 * * * * ?", jobSummary.getJobCron());
        Assert.assertEquals("Startup", jobSummary.getJobState());
    }

}
