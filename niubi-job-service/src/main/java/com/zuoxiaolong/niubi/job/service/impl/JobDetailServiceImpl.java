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

import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.JobDetail;
import com.zuoxiaolong.niubi.job.persistent.entity.JobRuntimeDetail;
import com.zuoxiaolong.niubi.job.scanner.JobScanClassLoader;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.RemoteJobScanner;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.service.JobDetailService;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobDetailServiceImpl extends AbstractService implements JobDetailService, ApplicationContextAware {

    @Autowired
    private BaseDao baseDao;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<JobDetail> getAllStandbyJobDetails() {
        return baseDao.getAll(JobDetail.class);
    }

    @Override
    public void save(String jarFilePath, String packagesToScan) {
        String jarFileName = jarFilePath.substring(jarFilePath.lastIndexOf("/") + 1);
        JobDetail param = new JobDetail();
        param.setJarFileName(jarFileName);
        List<JobDetail> jobDetailList = baseDao.getList(JobDetail.class, param);
        if (!ListHelper.isEmpty(jobDetailList)) {
            throw new ServiceException("This jar [" + jarFileName + "] has been uploaded before.");
        }

        JobScanClassLoader classLoader = new JobScanClassLoader(applicationContext.getClassLoader());
        classLoader.addJarFiles(jarFilePath);
        JobScanner jobScanner = new RemoteJobScanner(classLoader, packagesToScan, jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.getJobDescriptorList();
        for (JobDescriptor jobDescriptor : jobDescriptorList) {
            JobDetail jobDetail = new JobDetail();
            jobDetail.setGroupName(jobDescriptor.group());
            jobDetail.setJobName(jobDescriptor.name());
            jobDetail.setJarFileName(jarFileName);
            jobDetail.setPackagesToScan(packagesToScan);
            baseDao.save(jobDetail);

            JobRuntimeDetail jobRuntimeDetail = new JobRuntimeDetail();
            jobRuntimeDetail.setGroupName(jobDescriptor.group());
            jobRuntimeDetail.setJobName(jobDescriptor.name());
            JobRuntimeDetail jobRuntimeDetailInDb = baseDao.getUnique(JobRuntimeDetail.class, jobRuntimeDetail);
            if (jobRuntimeDetailInDb == null) {
                jobRuntimeDetail.setDefaultState();
                baseDao.save(jobRuntimeDetail);
            }
        }
    }

    @Override
    public List<String> getJarFileNameList(String group, String name) {
        JobDetail job = new JobDetail();
        job.setGroupName(group);
        job.setJobName(name);
        List<JobDetail> jobDetailList = baseDao.getList(JobDetail.class, job, false);
        List<String> jarFileNameList = new ArrayList<>();
        if (jobDetailList == null) {
            return jarFileNameList;
        }
        jarFileNameList.addAll(jobDetailList.stream().map(JobDetail::getJarFileName).collect(Collectors.toList()));
        return jarFileNameList;
    }

}
