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

import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJob;
import com.zuoxiaolong.niubi.job.service.StandbyJobService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class StandbyJobServiceImplTest extends AbstractSpringContextTest{

    @Autowired
    protected StandbyJobService standbyJobService;

    @Test
    public void getAllJobs() {
        List<StandbyJob> jobs = standbyJobService.getAllJobs();
        Assert.assertNotNull(jobs);
        Assert.assertTrue(jobs.size() == 0);
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        jobs = standbyJobService.getAllJobs();
        Assert.assertNotNull(jobs);
        Assert.assertTrue(jobs.size() == 2);
    }

    @Test
    public void getJob() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        StandbyJob job = standbyJobService.getJob("com.zuoxiaolong.niubi.job.sample.spring.job.Job1", "test", "niubi-job-sample-spring.jar");
        Assert.assertNotNull(job);
    }

    @Test
    public void getJarFileNameList() {
        standbyJobService.saveJob(getSampleJarFile(), "com.zuoxiaolong");
        List<String> jarFileNameList = standbyJobService.getJarFileNameList("com.zuoxiaolong.niubi.job.sample.spring.job.Job1", "test");
        Assert.assertNotNull(jarFileNameList);
        Assert.assertTrue(jarFileNameList.size() == 1);
        Assert.assertEquals("niubi-job-sample-spring.jar" , jarFileNameList.get(0));
    }

}
