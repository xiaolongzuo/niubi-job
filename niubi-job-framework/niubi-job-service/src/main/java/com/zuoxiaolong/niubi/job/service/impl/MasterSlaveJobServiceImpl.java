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
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJob;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoader;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.JobScannerFactory;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobService;
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
 * @since 0.9.3
 */
@Service
public class MasterSlaveJobServiceImpl extends AbstractService implements MasterSlaveJobService, ApplicationContextAware {

    @Autowired
    private BaseDao baseDao;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<MasterSlaveJob> getAllJobs() {
        return baseDao.getAll(MasterSlaveJob.class);
    }

    @Override
    public MasterSlaveJob getJob(String group, String name, String jarFileName) {
        MasterSlaveJob param = new MasterSlaveJob();
        param.setGroupName(group);
        param.setJobName(name);
        param.setJarFileName(jarFileName);
        return baseDao.getUnique(MasterSlaveJob.class, param);
    }

    @Override
    public void saveJob(String jarFilePath, String packagesToScan) {
        String jarFileName = JarFileHelper.getJarFileName(jarFilePath);
        MasterSlaveJob param = new MasterSlaveJob();
        param.setJarFileName(jarFileName);
        List<MasterSlaveJob> masterSlaveJobList = baseDao.getList(MasterSlaveJob.class, param);
        if (!ListHelper.isEmpty(masterSlaveJobList)) {
            throw new ServiceException("This jar [" + jarFileName + "] has been uploaded before.");
        }

        ApplicationClassLoader classLoader = ApplicationClassLoaderFactory.createNormalApplicationClassLoader(applicationContext.getClassLoader(), jarFilePath);
        JobScanner jobScanner = JobScannerFactory.createJarFileJobScanner(classLoader, packagesToScan, jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.getJobDescriptorList();
        String mode = jobScanner.hasSpringEnvironment() ? "Spring" : "Common";
        for (JobDescriptor jobDescriptor : jobDescriptorList) {
            MasterSlaveJob masterSlaveJob = new MasterSlaveJob();
            masterSlaveJob.setGroupName(jobDescriptor.group());
            masterSlaveJob.setJobName(jobDescriptor.name());
            masterSlaveJob.setJarFileName(jarFileName);
            masterSlaveJob.setPackagesToScan(packagesToScan);
            masterSlaveJob.setContainerType(mode);
            baseDao.save(masterSlaveJob);

            MasterSlaveJobSummary masterSlaveJobSummary = new MasterSlaveJobSummary();
            masterSlaveJobSummary.setGroupName(jobDescriptor.group());
            masterSlaveJobSummary.setJobName(jobDescriptor.name());
            MasterSlaveJobSummary masterSlaveJobSummaryInDb = baseDao.getUnique(MasterSlaveJobSummary.class, masterSlaveJobSummary);
            if (masterSlaveJobSummaryInDb == null) {
                masterSlaveJobSummary.setDefaultState();
                baseDao.save(masterSlaveJobSummary);
            }
        }
    }

    @Override
    public List<String> getJarFileNameList(String group, String name) {
        MasterSlaveJob job = new MasterSlaveJob();
        job.setGroupName(group);
        job.setJobName(name);
        List<MasterSlaveJob> masterSlaveJobList = baseDao.getList(MasterSlaveJob.class, job, false);
        List<String> jarFileNameList = new ArrayList<>();
        if (masterSlaveJobList == null) {
            return jarFileNameList;
        }
        jarFileNameList.addAll(masterSlaveJobList.stream().map(MasterSlaveJob::getJarFileName).collect(Collectors.toList()));
        return jarFileNameList;
    }

}
