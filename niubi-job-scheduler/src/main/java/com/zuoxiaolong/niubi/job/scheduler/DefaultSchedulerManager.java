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
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xiaolong Zuo
 * @since 16/1/10 01:09
 */
public class DefaultSchedulerManager implements SchedulerManager {

    private Scheduler scheduler;

    private Map<String, List<String>> groupNameListMap;

    private List<String> groupList;

    private Map<String, ScheduleStatus> jobStatusMap;

    public DefaultSchedulerManager(JobBeanFactory jobBeanFactory, List<JobDescriptor> jobDescriptorList) {
        Properties properties = new Properties();
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("com/zuoxiaolong/niubi/job/scheduler/quartz-default.properties"));
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("quartz.properties"));
        } catch (IOException e) {
            LoggerHelper.warn("quartz properties not found ,use default instead.");
        }
        AssertHelper.notNull(properties, "configuration can't be null.");
        AssertHelper.notNull(jobBeanFactory, "jobBeanFactory can't be null.");
        AssertHelper.notNull(jobDescriptorList, "jobDescriptorList can't be null.");
        initScheduler(properties);
        initJobDetails(jobBeanFactory, jobDescriptorList);
    }

    public DefaultSchedulerManager(Properties properties, JobBeanFactory jobBeanFactory, List<JobDescriptor> jobDescriptorList) {
        AssertHelper.notNull(properties, "configuration can't be null.");
        AssertHelper.notNull(jobBeanFactory, "jobBeanFactory can't be null.");
        AssertHelper.notNull(jobDescriptorList, "jobDescriptorList can't be null.");
        initScheduler(properties);
        initJobDetails(jobBeanFactory, jobDescriptorList);
    }

    protected void initScheduler(Properties properties) {
        this.groupNameListMap = new ConcurrentHashMap<>();
        this.groupList = new ArrayList<>();
        this.jobStatusMap = new ConcurrentHashMap<>();
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            schedulerFactory.initialize(properties);
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            LoggerHelper.error("create core failed.", e);
            throw new NiubiException(e);
        }
    }

    protected void initJobDetails(JobBeanFactory jobBeanFactory, List<JobDescriptor> jobDescriptorList) {
        for (JobDescriptor descriptor : jobDescriptorList) {
            addJobDetail(new DefaultSchedulerJobDescriptor(descriptor));
        }
        try {
            scheduler.getContext().put(JobBeanFactory.DATA_MAP_KEY, jobBeanFactory);
        } catch (SchedulerException e) {
            LoggerHelper.error("get schedule context failed.", e);
            throw new NiubiException(e);
        }
    }

    protected void addJobDetail(SchedulerJobDescriptor descriptor) {
        try {
            scheduler.addJob(descriptor.putJobData(SchedulerJobDescriptor.DATA_MAP_KEY, descriptor).jobDetail(), true);
        } catch (SchedulerException e) {
            LoggerHelper.error("add job failed.", e);
            throw new NiubiException(e);
        }
        List<String> jobKeyList = groupNameListMap.get(descriptor.group());
        if (jobKeyList == null) {
            jobKeyList = new ArrayList<>();
        }
        jobKeyList.add(descriptor.name());
        groupNameListMap.put(descriptor.group(), jobKeyList);
        jobStatusMap.put(getUniqueId(descriptor.jobKey()), ScheduleStatus.SHUTDOWN);
        if (!groupList.contains(descriptor.group())) {
            groupList.add(descriptor.group());
        }
    }

    protected String getUniqueId(JobKey jobKey) {
        return jobKey.getGroup() + "." + jobKey.getName();
    }

    public List<String> getNameList(String group) {
        return Collections.unmodifiableList(groupNameListMap.get(group));
    }

    @Override
    public synchronized ScheduleStatus getScheduleStatus(String group, String name) {
        return jobStatusMap.get(getUniqueId(JobKey.jobKey(name, group)));
    }

    public List<String> getGroupList() {
        return Collections.unmodifiableList(groupList);
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
        if (scheduleStatus == ScheduleStatus.SHUTDOWN) {
            LoggerHelper.info("job [" + group + "," + name + "] now is shutdown ,begin startup.");
            SchedulerJobDescriptor jobDescriptor;
            try {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                jobDescriptor = JobDataMapManager.getJobDescriptor(jobDetail);
                if (jobDescriptor.isManualTrigger()) {
                    LoggerHelper.error("job need to trigger manual : " + JsonHelper.toJson(jobDescriptor));
                    return;
                }
            } catch (SchedulerException e) {
                LoggerHelper.error("get jobDescriptor [" + group + "," + name + "] job failed.", e);
                return;
            }
            try {
                scheduler.scheduleJob(jobDescriptor.trigger());
                LoggerHelper.info("job [" + group + "," + name + "] has been started successfully.");
            } catch (SchedulerException e) {
                LoggerHelper.error("startup [" + group + "," + name + "] job failed.", e);
                return;
            }
        } else if (scheduleStatus == ScheduleStatus.PAUSE) {
            try {
                scheduler.resumeJob(jobKey);
                LoggerHelper.info("job [" + group + "," + name + "] has been resumed.");
            } catch (SchedulerException e) {
                LoggerHelper.error("resume [" + group + "," + name + "] job failed.", e);
                return;
            }
        } else {
            LoggerHelper.warn("job [" + group + "," + name + "] has been started, skip.");
        }
        jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.STARTUP);
    }

    @Override
    public synchronized void startupManual(String cron, String misfirePolicy) {
        for (String group : getGroupList()) {
            startupManual(group, cron, misfirePolicy);
        }
    }

    @Override
    public synchronized void startupManual(String group, String cron, String misfirePolicy) {
        for (String name : getNameList(group)) {
            startupManual(group, name, cron, misfirePolicy);
        }
    }

    @Override
    public synchronized void startupManual(String group, String name, String cron, String misfirePolicy) {
        JobKey jobKey = JobKey.jobKey(name, group);
        ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
        SchedulerJobDescriptor jobDescriptor;
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDescriptor = JobDataMapManager.getJobDescriptor(jobDetail);
        } catch (SchedulerException e) {
            LoggerHelper.error("get jobDescriptor [" + group + "," + name + "] job failed.", e);
            return;
        }
        if (scheduleStatus == ScheduleStatus.SHUTDOWN) {
            LoggerHelper.info("job [" + group + "," + name + "] now is shutdown ,begin startup.");
            try {
                scheduler.scheduleJob(jobDescriptor.withTrigger(cron, misfirePolicy).trigger());
                LoggerHelper.info("job [" + group + "," + name + "] has been started successfully.");
            } catch (SchedulerException e) {
                LoggerHelper.error("startup [" + group + "," + name + "," + scheduleStatus + "] job failed.", e);
                return;
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
                return;
            }
        }
        jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.STARTUP);
    }

    public synchronized void shutdown() {
        getGroupList().forEach(this::shutdown);
    }

    public synchronized void shutdown(String group) {
        for (String name : getNameList(group)) {
            shutdown(group, name);
        }
    }

    @Override
    public synchronized void shutdown(String group, String name) {
        JobKey jobKey = JobKey.jobKey(name, group);
        ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
        if (scheduleStatus != ScheduleStatus.STARTUP) {
            LoggerHelper.warn("group [" + group + "," + name + "] has been paused.");
        } else {
            try {
                scheduler.pauseJob(jobKey);
                LoggerHelper.info("group [" + group + "," + name + "] has been paused successfully.");
            } catch (SchedulerException e) {
                LoggerHelper.error("pause [" + group + "," + name + "] job failed.", e);
                return;
            }
            jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.PAUSE);
        }
    }

}