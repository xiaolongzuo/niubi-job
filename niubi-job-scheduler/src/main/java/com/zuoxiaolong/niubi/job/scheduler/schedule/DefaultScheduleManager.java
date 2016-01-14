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

package com.zuoxiaolong.niubi.job.scheduler.schedule;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scheduler.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scheduler.context.Context;
import com.zuoxiaolong.niubi.job.scheduler.job.JobDataMapManager;
import com.zuoxiaolong.niubi.job.scheduler.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scheduler.scanner.LocalJobScanner;
import com.zuoxiaolong.niubi.job.scheduler.scanner.RemoteJobScanner;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Xiaolong Zuo
 * @since 16/1/10 01:09
 */
public class DefaultScheduleManager implements ScheduleManager {

    private ReentrantLock lock = new ReentrantLock();

    private JobScanner jobScanner;

    private Scheduler scheduler;

    private Map<String, List<String>> groupNameListMap;

    private List<String> groupList;

    private Map<String, ScheduleStatus> jobStatusMap;

    public DefaultScheduleManager(Context context) {
        initScheduler(context);
        this.jobScanner = new LocalJobScanner(context);
        initJobDetails(context);
    }

    public DefaultScheduleManager(Context context, String[] jarUrls) {
        initScheduler(context);
        this.jobScanner = new RemoteJobScanner(context, jarUrls);
        initJobDetails(context);
    }

    protected void initScheduler(Context context) {
        this.groupNameListMap = new ConcurrentHashMap<>();
        this.groupList = new ArrayList<>();
        this.jobStatusMap = new ConcurrentHashMap<>();
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            schedulerFactory.initialize(context.configuration().getProperties());
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            LoggerHelper.error("create core failed.", e);
            throw new NiubiException(e);
        }
    }

    protected void initJobDetails(Context context) {
        List<JobDescriptor> descriptorList = jobScanner.scan();
        descriptorList.forEach(this::addJobDetail);
        try {
            scheduler.getContext().put(Context.DATA_MAP_KEY, context);
        } catch (SchedulerException e) {
            LoggerHelper.error("get schedule context failed.", e);
            throw new NiubiException(e);
        }
    }

    protected void addJobDetail(JobDescriptor descriptor) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JobDescriptor.DATA_MAP_KEY, descriptor);
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

    public List<String> getGroupList() {
        return Collections.unmodifiableList(groupList);
    }

    public void startup() {
        lock.lock();
        try {
            getGroupList().forEach(this::startup);
        } finally {
            lock.unlock();
        }
    }

    public void startup(String group) {
        lock.lock();
        try {
            for (String name : getNameList(group)) {
                startup(group, name);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startup(String group, String name) {
        lock.lock();
        try {
            JobKey jobKey = JobKey.jobKey(name, group);
            ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
            if (scheduleStatus == ScheduleStatus.SHUTDOWN) {
                LoggerHelper.info("job [" + group + "," + name + "] now is shutdown ,begin startup.");
                JobDescriptor jobDescriptor;
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startup(String cron, MisfirePolicy misfirePolicy) {
        lock.lock();
        try {
            for (String group : getGroupList()) {
                startup(group, cron, misfirePolicy);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startup(String group, String cron, MisfirePolicy misfirePolicy) {
        lock.lock();
        try {
            for (String name : getNameList(group)) {
                startup(group, name, cron, misfirePolicy);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void startup(String group, String name, String cron, MisfirePolicy misfirePolicy) {
        lock.lock();
        try {
            JobKey jobKey = JobKey.jobKey(name, group);
            ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
            JobDescriptor jobDescriptor;
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
                    LoggerHelper.error("startup [" + group + "," + name + "] job failed.", e);
                    return;
                }
            } else {
                try {
                    scheduler.rescheduleJob(jobDescriptor.triggerKey(), jobDescriptor.withTrigger(cron, misfirePolicy).trigger());
                    LoggerHelper.info("job [" + group + "," + name + "] has been rescheduled.");
                } catch (SchedulerException e) {
                    LoggerHelper.error("reschedule [" + group + "," + name + "] job failed.", e);
                    return;
                }
            }
            jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.STARTUP);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            getGroupList().forEach(this::shutdown);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown(String group) {
        lock.lock();
        try {
            for (String name : getNameList(group)) {
                shutdown(group, name);
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void shutdown(String group, String name) {
        lock.lock();
        try {
            JobKey jobKey = JobKey.jobKey(name, group);
            ScheduleStatus scheduleStatus = jobStatusMap.get(getUniqueId(jobKey));
            if (scheduleStatus != ScheduleStatus.STARTUP) {
                LoggerHelper.warn("group [" + group + "] has been paused.");
            } else {
                try {
                    scheduler.pauseJob(jobKey);
                    LoggerHelper.info("group [" + group + "] has been paused successfully.");
                } catch (SchedulerException e) {
                    LoggerHelper.error("pause [" + group + "] job failed.", e);
                    return;
                }
            }
            jobStatusMap.put(getUniqueId(jobKey), ScheduleStatus.PAUSE);
        } finally {
            lock.unlock();
        }
    }

}