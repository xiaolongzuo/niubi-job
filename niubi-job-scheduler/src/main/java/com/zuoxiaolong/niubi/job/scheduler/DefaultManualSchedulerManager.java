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
import org.quartz.JobKey;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 手动控制的调度管理器默认实现,用于与Console交互.
 *
 * @author Xiaolong Zuo
 * @since 0.9.4
 */
public class DefaultManualSchedulerManager extends AbstractSchedulerManager implements ManualSchedulerManager {

    private Map<String, JobBeanFactory> jobBeanFactoryMap;

    private Map<String, List<JobDescriptor>> jobDescriptorListMap;

    public DefaultManualSchedulerManager(Properties properties) {
        initScheduler(properties);
        SchedulerContext schedulerContext;
        try {
            schedulerContext = this.scheduler.getContext();
        } catch (SchedulerException e) {
            throw new NiubiException(e);
        }
        this.jobBeanFactoryMap = new HashMap<>();
        this.jobDescriptorListMap = new HashMap<>();
        schedulerContext.put("jobBeanFactoryMap", jobBeanFactoryMap);
        schedulerContext.put("jobDescriptorListMap", jobDescriptorListMap);
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
        try {
            createJobRuntimeEnv(jarFilePath, packagesToScan, isSpring);
        } catch (Exception e) {
            throw new NiubiException(e);
        }
        JobKey jobKey = JobKey.jobKey(name, group);
        ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
        SchedulerJobDescriptor jobDescriptor;
        try {
            jobDescriptor = findSchedulerJobDescriptor(jarFilePath, group, name);
            scheduler.addJob(jobDescriptor.putJobData(JobDescriptor.DATA_MAP_KEY, jobDescriptor).putJobData("jarFilePath", jarFilePath).jobDetail(), true);
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

    protected SchedulerJobDescriptor findSchedulerJobDescriptor(String jarFilePath, String group, String name) {
        List<JobDescriptor> jobDescriptorList = jobDescriptorListMap.get(jarFilePath);
        if (jobDescriptorList == null) {
            throw new NiubiException(new IllegalStateException("job descriptor list can't be null."));
        }
        for (JobDescriptor jobDescriptorInner : jobDescriptorList) {
            if (jobDescriptorInner.group().equals(group) && jobDescriptorInner.name().equals(name)) {
                return (SchedulerJobDescriptor) jobDescriptorInner;
            }
        }
        throw new NiubiException(new RuntimeException("can't find SchedulerJobDescriptor for [" + group + "." + name + "]"));
    }

    /**
     * 创建Job的运行时环境,加载相应的Jar包资源.
     *
     * @param jarFilePath jar包本地路径
     * @param packagesToScan 需要扫描的包
     * @param isSpring 是否spring环境
     * @throws Exception
     */
    protected void createJobRuntimeEnv(String jarFilePath, String packagesToScan, boolean isSpring) throws Exception {
        JobBeanFactory jobBeanFactory = jobBeanFactoryMap.get(jarFilePath);
        if (jobBeanFactory != null) {
            return;
        }
        synchronized (jobBeanFactoryMap) {
            jobBeanFactory = jobBeanFactoryMap.get(jarFilePath);
            if (jobBeanFactory != null) {
                return;
            }
            jobBeanFactory = createJobBeanFactory(jarFilePath, isSpring);
            jobBeanFactoryMap.put(jarFilePath, jobBeanFactory);
            ClassLoader classLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
            JobScanner jobScanner = JobScannerFactory.createJarFileJobScanner(classLoader, packagesToScan, jarFilePath);
            jobDescriptorListMap.put(jarFilePath, jobScanner.getJobDescriptorList());
        }
    }

    /**
     * 创建缓存JobBean实例的工厂
     *
     * @param jarFilePath Jar包本地路径
     * @param isSpring 是否spring环境
     * @return 创建好的JobBean工厂
     * @throws Exception
     */
    protected JobBeanFactory createJobBeanFactory(String jarFilePath, boolean isSpring) throws Exception {
        String jobBeanFactoryClassName;
        if (isSpring) {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory";
        } else {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory";
        }
        ClassLoader jarApplicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        Class<? extends JobBeanFactory> jobBeanFactoryClass = (Class<? extends JobBeanFactory>) jarApplicationClassLoader.loadClass(jobBeanFactoryClassName);
        Class<?>[] parameterTypes = new Class[]{ClassLoader.class};
        Constructor<? extends JobBeanFactory> jobBeanFactoryConstructor = jobBeanFactoryClass.getConstructor(parameterTypes);
        return jobBeanFactoryConstructor.newInstance(jarApplicationClassLoader);
    }

}