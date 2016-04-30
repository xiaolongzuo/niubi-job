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
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 自动调度管理器的默认实现
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class DefaultAutomaticScheduleManager extends AbstractScheduleManager implements AutomaticScheduleManager {

    private List<JobDescriptor> jobDescriptorList;

    public DefaultAutomaticScheduleManager(JobBeanFactory jobBeanFactory, List<JobDescriptor> jobDescriptorList) {
        this.jobDescriptorList = Collections.unmodifiableList(jobDescriptorList);
        initScheduler(loadProperties());
        JobDataMapManager.initAutomaticScheduler(scheduler, jobBeanFactory);
        initJobDetails();
    }

    protected Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("com/zuoxiaolong/niubi/job/scheduler/quartz-default.properties"));
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("quartz.properties"));
        } catch (Exception e) {
            LoggerHelper.warn("quartz properties not found ,use default instead.");
        }
        return properties;
    }

    protected void initJobDetails() {
        for (JobDescriptor descriptor : jobDescriptorList) {
            addJobDetail(new DefaultScheduleJobDescriptor(descriptor));
        }
    }

    protected void addJobDetail(ScheduleJobDescriptor descriptor) {
        try {
            scheduler.addJob(descriptor.jobDetail(), true);
        } catch (SchedulerException e) {
            LoggerHelper.error("add job failed.", e);
            throw new NiubiException(e);
        }
        List<String> jobKeyList = groupNameListMap.get(descriptor.group());
        if (jobKeyList == null) {
            jobKeyList = new ArrayList<>();
        }
        if (!jobKeyList.contains(descriptor.name())) {
            jobKeyList.add(descriptor.name());
        }
        groupNameListMap.put(descriptor.group(), jobKeyList);
        if (!groupList.contains(descriptor.group())) {
            groupList.add(descriptor.group());
        }
    }

    public synchronized void startup() {
        getGroupList().forEach(this::startup);
    }

    public synchronized void startup(String group) {
        for (String name : getNameList(group)) {
            startup(group, name);
        }
    }

    @Override
    public synchronized void startup(String group, String name) {
        JobKey jobKey = JobKey.jobKey(name, group);
        ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
        if (scheduleStatus == null || scheduleStatus == ScheduleStatus.SHUTDOWN) {
            LoggerHelper.info("job [" + group + "," + name + "] now is shutdown ,begin startup.");
            startupJob(jobKey);
        } else if (scheduleStatus == ScheduleStatus.PAUSE) {
            LoggerHelper.info("job [" + group + "," + name + "] now is pause ,begin resume.");
            resumeJob(jobKey);
        } else {
            LoggerHelper.warn("job [" + group + "," + name + "] has been startup, skip.");
        }
        jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.STARTUP);
    }

    private void startupJob(JobKey jobKey) {
        ScheduleJobDescriptor jobDescriptor;
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDescriptor = JobDataMapManager.getJobDescriptor(jobDetail);
            if (jobDescriptor.isManualTrigger()) {
                LoggerHelper.error("job need to trigger manual : " + JsonHelper.toJson(jobDescriptor));
                throw new NiubiException(new IllegalArgumentException("job need to trigger manual : " + JsonHelper.toJson(jobDescriptor)));
            }
        } catch (SchedulerException e) {
            LoggerHelper.error("get jobDescriptor [" + jobKey.getGroup() + "," + jobKey.getName() + "] job failed.", e);
            throw new NiubiException(e);
        }
        try {
            scheduler.scheduleJob(jobDescriptor.trigger());
            LoggerHelper.info("job [" + jobKey.getGroup() + "," + jobKey.getName() + "] has been started successfully.");
        } catch (SchedulerException e) {
            LoggerHelper.error("startup [" + jobKey.getGroup() + "," + jobKey.getName() + "] job failed.", e);
            throw new NiubiException(e);
        }
    }

    private void resumeJob(JobKey jobKey) {
        try {
            scheduler.resumeJob(jobKey);
            LoggerHelper.info("job [" + jobKey.getGroup() + "," + jobKey.getName() + "] has been resumed.");
        } catch (SchedulerException e) {
            LoggerHelper.error("resume [" + jobKey.getGroup() + "," + jobKey.getName() + "] job failed.", e);
            throw new NiubiException(e);
        }
    }

}