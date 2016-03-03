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

package com.zuoxiaolong.niubi.job.scheduler;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.JobScannerFactory;
import com.zuoxiaolong.niubi.job.scanner.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4
 */
public class DefaultManualSchedulerManager extends AbstractSchedulerManager implements ManualSchedulerManager {

    private Map<String, JobBeanFactory> jobBeanFactoryMap;

    private Map<String, List<JobDescriptor>> jobDescriptorListMap;

    public DefaultManualSchedulerManager(Properties properties) {
        initScheduler(properties);
    }

    @Override
    public synchronized void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String cron, String misfirePolicy) {
        for (String group : getGroupList()) {
            startupManual(jarFilePath, packagesToScan, isSpring, group, cron, misfirePolicy);
        }
    }

    @Override
    public synchronized void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String group, String cron, String misfirePolicy) {
        for (String name : getNameList(group)) {
            startupManual(jarFilePath, packagesToScan, isSpring, group, name, cron, misfirePolicy);
        }
    }

    @Override
    public synchronized void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String group, String name, String cron, String misfirePolicy) {
        JobKey jobKey = JobKey.jobKey(name, group);
        ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
        SchedulerJobDescriptor jobDescriptor;
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDescriptor = JobDataMapManager.getJobDescriptor(jobDetail);
        } catch (SchedulerException e) {
            LoggerHelper.error("get jobDescriptor [" + group + "," + name + "] job failed.", e);
            throw new NiubiException(e);
        }
        if (scheduleStatus == null || scheduleStatus == ScheduleStatus.SHUTDOWN) {
            LoggerHelper.info("job [" + group + "," + name + "] now is shutdown ,begin startup.");
            try {
                scheduler.scheduleJob(jobDescriptor.withTrigger(cron, misfirePolicy).trigger());
                LoggerHelper.info("job [" + group + "," + name + "] has been started successfully.");
            } catch (SchedulerException e) {
                LoggerHelper.error("startup [" + group + "," + name + "," + scheduleStatus + "] job failed.", e);
                throw new NiubiException(e);
            }
        } else {
            try {
                CronTrigger trigger = (CronTrigger) scheduler.getTrigger(jobDescriptor.triggerKey());
                if (!trigger.getCronExpression().equals(cron) || trigger.getMisfireInstruction() != MisfirePolicy.valueOf(misfirePolicy).getIntValue()) {
                    scheduler.rescheduleJob(jobDescriptor.triggerKey(), jobDescriptor.withTrigger(cron, misfirePolicy).trigger());
                } else {
                    scheduler.resumeJob(jobKey);
                }
                LoggerHelper.info("job [" + group + "," + name + "] has been rescheduled.");
            } catch (SchedulerException e) {
                LoggerHelper.error("reschedule [" + group + "," + name + "," + scheduleStatus + "] job failed.", e);
                throw new NiubiException(e);
            }
        }
        jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.STARTUP);
    }

    protected JobBeanFactory createJobBeanFactory(String jarFilePath, String packagesToScan, boolean isSpring) throws Exception {
        String jobBeanFactoryClassName;
        if (isSpring) {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory";
        } else {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory";
        }
        ClassLoader jarApplicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        JobScanner jobScanner = JobScannerFactory.createJarFileJobScanner(jarApplicationClassLoader, packagesToScan, jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.getJobDescriptorList();
        Class<? extends JobBeanFactory> jobBeanFactoryClass = (Class<? extends JobBeanFactory>) jarApplicationClassLoader.loadClass(jobBeanFactoryClassName);
        Class<?>[] parameterTypes = new Class[]{ClassLoader.class};
        Constructor<? extends JobBeanFactory> jobBeanFactoryConstructor = jobBeanFactoryClass.getConstructor(parameterTypes);
        return jobBeanFactoryConstructor.newInstance(jarApplicationClassLoader);
    }

}