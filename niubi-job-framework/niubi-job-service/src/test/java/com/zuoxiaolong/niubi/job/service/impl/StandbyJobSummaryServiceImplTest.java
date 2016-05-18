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

import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.StandbyApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobSummary;
import com.zuoxiaolong.niubi.job.service.StandbyJobService;
import com.zuoxiaolong.niubi.job.service.StandbyJobSummaryService;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class StandbyJobSummaryServiceImplTest extends AbstractSpringContextTest{

    @Autowired
    private StandbyJobSummaryService standbyJobSummaryService;

    @Autowired
    private StandbyJobService standbyJobService;

    @Autowired
    private BaseDao baseDao;

    @Autowired
    private CuratorFramework client;

    @Test
    public void getAllJobSummaries() {
        List<StandbyJobSummary> jobSummaries = standbyJobSummaryService.getAllJobSummaries();
        Assert.assertNotNull(jobSummaries);
        Assert.assertTrue(jobSummaries.size() == 0);
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        jobSummaries = standbyJobSummaryService.getAllJobSummaries();
        Assert.assertNotNull(jobSummaries);
        Assert.assertTrue(jobSummaries.size() == 2);
    }

    @Test
    public void saveJobSummary() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<StandbyJobSummary> jobSummaries = standbyJobSummaryService.getAllJobSummaries();
        StandbyJobSummary jobSummary = jobSummaries.get(0);
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setOriginalJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setJobOperation("Start");
        jobSummary.setContainerType("Spring");
        standbyJobSummaryService.saveJobSummary(jobSummary);
        jobSummary = baseDao.get(StandbyJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("Executing", jobSummary.getJobState());
    }

    @Test
    public void getJobSummary() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<StandbyJobSummary> jobSummaries = standbyJobSummaryService.getAllJobSummaries();
        StandbyJobSummary jobSummary = jobSummaries.get(0);
        String id = jobSummary.getId();
        jobSummary.setJobCron("0 * * * * ?");
        jobSummary.setMisfirePolicy("None");
        jobSummary.setJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setOriginalJarFileName("niubi-job-sample-spring.jar");
        jobSummary.setJobOperation("Start");
        jobSummary.setContainerType("Spring");
        standbyJobSummaryService.saveJobSummary(jobSummary);
        jobSummary = standbyJobSummaryService.getJobSummary(id);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getJobState(), "Shutdown");
        StandbyApiFactory apiFactory = new StandbyApiFactoryImpl(client);
        StandbyJobData.Data data = new StandbyJobData.Data();
        data.setJobState("Startup");
        apiFactory.jobApi().updateJob(jobSummary.getGroupName(), jobSummary.getJobName(), data);
        jobSummary = standbyJobSummaryService.getJobSummary(id);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getJobState(), "Startup");
    }

    @Test
    public void updateJobSummary1() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        StandbyJobSummary jobSummary = standbyJobSummaryService.getAllJobSummaries().get(0);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getGroupName(), "com.zuoxiaolong.niubi.job.sample.spring.job.Job1");
        Assert.assertEquals(jobSummary.getJobName(), "test");
        StandbyJobData.Data data = new StandbyJobData.Data();
        data.setJobCron("0 * * * * ?");
        data.setGroupName(jobSummary.getGroupName());
        data.setJobName(jobSummary.getJobName());
        standbyJobSummaryService.updateJobSummary(data);
        jobSummary = baseDao.get(StandbyJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("0 * * * * ?", jobSummary.getJobCron());
    }

    @Test
    public void updateJobSummary2() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        StandbyJobSummary jobSummary = standbyJobSummaryService.getAllJobSummaries().get(0);
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals(jobSummary.getGroupName(), "com.zuoxiaolong.niubi.job.sample.spring.job.Job1");
        Assert.assertEquals(jobSummary.getJobName(), "test");
        standbyJobSummaryService.saveJobSummary(jobSummary);
        StandbyApiFactory apiFactory = new StandbyApiFactoryImpl(client);
        StandbyJobData.Data data = new StandbyJobData.Data();
        data.setJobState("Startup");
        data.setJobCron("0 * * * * ?");
        data.setGroupName(jobSummary.getGroupName());
        data.setJobName(jobSummary.getJobName());
        apiFactory.jobApi().updateJob(jobSummary.getGroupName(), jobSummary.getJobName(), data);
        standbyJobSummaryService.updateJobSummary(jobSummary.getId());
        jobSummary = baseDao.get(StandbyJobSummary.class, jobSummary.getId());
        Assert.assertNotNull(jobSummary);
        Assert.assertEquals("0 * * * * ?", jobSummary.getJobCron());
        Assert.assertEquals("Startup", jobSummary.getJobState());
    }

}
