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

import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.Job;
import com.zuoxiaolong.niubi.job.persistent.entity.JobJar;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.RemoteJobScanner;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.service.JobJarService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobJarServiceImpl extends AbstractService implements JobJarService, ApplicationContextAware {

    @Autowired
    private BaseDao baseDao;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void save(String jarFilePath) {
        JobJar jobJar = new JobJar();
        jobJar.setJarFileName(jarFilePath.substring(jarFilePath.lastIndexOf("/") + 1));
        List<Job> jobs = new ArrayList<>();
        JobScanner jobScanner = new RemoteJobScanner(applicationContext.getClassLoader(), jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.scan();
        for (JobDescriptor jobDescriptor : jobDescriptorList) {
            Job job = new Job();
            job.setGroupName(jobDescriptor.group());
            job.setJobName(jobDescriptor.name());
            job.setJobJar(jobJar);
            job.setCron(jobDescriptor.cron());
            job.setMisfirePolicy(jobDescriptor.misfirePolicy().name());
            jobs.add(job);
        }
        jobJar.setJobs(jobs);
        baseDao.save(jobJar);
    }

}
