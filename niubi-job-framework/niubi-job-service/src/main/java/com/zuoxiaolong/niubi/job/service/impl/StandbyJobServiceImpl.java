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

import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJob;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobSummary;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoader;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.JobScannerFactory;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import com.zuoxiaolong.niubi.job.service.StandbyJobService;
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
 * @since 0.9.3
 */
@Service
public class StandbyJobServiceImpl extends AbstractService implements StandbyJobService, ApplicationContextAware {

    @Autowired
    private BaseDao baseDao;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<StandbyJob> getAllJobs() {
        return baseDao.getAll(StandbyJob.class);
    }

    @Override
    public StandbyJob getJob(String group, String name, String jarFileName) {
        StandbyJob param = new StandbyJob();
        param.setGroupName(group);
        param.setJobName(name);
        param.setJarFileName(jarFileName);
        return baseDao.getUnique(StandbyJob.class, param);
    }

    @Override
    public void saveJob(String jarFilePath, String packagesToScan) {
        String jarFileName = JarFileHelper.getJarFileName(jarFilePath);
        StandbyJob param = new StandbyJob();
        param.setJarFileName(jarFileName);
        List<StandbyJob> standbyJobList = baseDao.getList(StandbyJob.class, param);
        if (!ListHelper.isEmpty(standbyJobList)) {
            throw new ServiceException("This jar [" + jarFileName + "] has been uploaded before.");
        }

        ApplicationClassLoader classLoader = ApplicationClassLoaderFactory.createNormalApplicationClassLoader(applicationContext.getClassLoader(), jarFilePath);
        JobScanner jobScanner = JobScannerFactory.createJarFileJobScanner(classLoader, packagesToScan, jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.getJobDescriptorList();
        String mode = jobScanner.hasSpringEnvironment() ? "Spring" : "Common";
        for (JobDescriptor jobDescriptor : jobDescriptorList) {
            StandbyJob standbyJob = new StandbyJob();
            standbyJob.setGroupName(jobDescriptor.group());
            standbyJob.setJobName(jobDescriptor.name());
            standbyJob.setJarFileName(jarFileName);
            standbyJob.setPackagesToScan(packagesToScan);
            standbyJob.setContainerType(mode);
            baseDao.save(standbyJob);

            StandbyJobSummary standbyJobSummary = new StandbyJobSummary();
            standbyJobSummary.setGroupName(jobDescriptor.group());
            standbyJobSummary.setJobName(jobDescriptor.name());
            StandbyJobSummary standbyJobSummaryInDb = baseDao.getUnique(StandbyJobSummary.class, standbyJobSummary);
            if (standbyJobSummaryInDb == null) {
                standbyJobSummary.setDefaultState();
                baseDao.save(standbyJobSummary);
            }
        }
    }

    @Override
    public List<String> getJarFileNameList(String group, String name) {
        StandbyJob job = new StandbyJob();
        job.setGroupName(group);
        job.setJobName(name);
        List<StandbyJob> standbyJobList = baseDao.getList(StandbyJob.class, job, false);
        List<String> jarFileNameList = new ArrayList<>();
        if (standbyJobList == null) {
            return jarFileNameList;
        }
        jarFileNameList.addAll(standbyJobList.stream().map(StandbyJob::getJarFileName).collect(Collectors.toList()));
        return jarFileNameList;
    }

}
