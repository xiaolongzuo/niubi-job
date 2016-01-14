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
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scheduler.config.Context;
import com.zuoxiaolong.niubi.job.scheduler.job.JobDataMapManager;
import com.zuoxiaolong.niubi.job.scheduler.job.JobTriggerFactory;
import com.zuoxiaolong.niubi.job.scheduler.job.MethodDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.job.TriggerDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.scanner.MethodTriggerDescriptor;
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

    private Scheduler scheduler;

    private Map<String, List<JobKey>> jobKeyListMap;

    private List<String> sortedGroupList;

    private Map<String, ScheduleStatus> groupStatusMap;

    public DefaultScheduleManager(Context context) {
        this.jobKeyListMap = new ConcurrentHashMap<String, List<JobKey>>();
        this.sortedGroupList = new ArrayList<String>();
        this.groupStatusMap = new ConcurrentHashMap<String, ScheduleStatus>();
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

    public void startup() {
        lock.lock();
        try {
            for (String group : getGroupList()) {
                startup(group);
            }
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        lock.lock();
        try {
            for (String group : getGroupList()) {
                shutdown(group);
            }
        } finally {
            lock.unlock();
        }
    }

    public void startup(String group) {
        lock.lock();
        try {
            ScheduleStatus scheduleStatus = groupStatusMap.get(group);
            if (scheduleStatus == ScheduleStatus.SHUTDOWN) {
                LoggerHelper.info("group [" + group + "] now is shutdown ,begin startup.");
                scheduleJob(group);
                LoggerHelper.info("group [" + group + "] has been started successfully.");
            } else if (scheduleStatus == ScheduleStatus.PAUSE) {
                for (JobKey jobKey : getJobKeyList(group)) {
                    try {
                        scheduler.resumeJob(jobKey);
                    } catch (SchedulerException e) {
                        LoggerHelper.error("resume [" + group + "] job failed.", e);
                    }
                }
                LoggerHelper.warn("group [" + group + "] has been resumed.");
            } else {
                LoggerHelper.warn("group [" + group + "] has been started, skip.");
            }
            groupStatusMap.put(group, ScheduleStatus.STARTUP);
        } finally {
            lock.unlock();
        }
    }

    private void scheduleJob(String group) {
        List<JobKey> jobKeyList = jobKeyListMap.get(group);
        for (JobKey jobKey : jobKeyList) {
            try {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                TriggerDescriptor triggerDescriptor = JobDataMapManager.getTriggerDescriptor(jobDetail);
                scheduler.scheduleJob(triggerDescriptor.trigger());
            } catch (SchedulerException e) {
                LoggerHelper.error("trigger job failed.", e);
                throw new NiubiException(e);
            }
        }
    }

    public void shutdown(String group) {
        lock.lock();
        try {
            ScheduleStatus scheduleStatus = groupStatusMap.get(group);
            if (scheduleStatus != ScheduleStatus.STARTUP) {
                LoggerHelper.warn("group [" + group + "] has been paused.");
            } else {
                for (JobKey jobKey : getJobKeyList(group)) {
                    try {
                        scheduler.pauseJob(jobKey);
                    } catch (SchedulerException e) {
                        LoggerHelper.error("pause [" + group + "] job failed.", e);
                    }
                }
                groupStatusMap.put(group, ScheduleStatus.PAUSE);
                LoggerHelper.warn("group [" + group + "] has been paused successfully.");
            }
        } finally {
            lock.unlock();
        }
    }

    public ScheduleStatus getScheduleStatus(String group) {
        return groupStatusMap.get(group);
    }

    public void bindContext(Context context) {
        try {
            scheduler.getContext().put(Context.DATA_MAP_KEY, context);
        } catch (SchedulerException e) {
            LoggerHelper.error("get schedule context failed.", e);
            throw new NiubiException(e);
        }
    }

    public List<JobKey> getJobKeyList(String group) {
        return Collections.unmodifiableList(jobKeyListMap.get(group));
    }

    public List<String> getGroupList() {
        return Collections.unmodifiableList(sortedGroupList);
    }

    public void addJob(MethodTriggerDescriptor descriptor) {
        JobKey jobKey = descriptor.jobKey();
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(TriggerDescriptor.DATA_MAP_KEY, descriptor.getTriggerDescriptor());
        jobDataMap.put(MethodDescriptor.DATA_MAP_KEY, descriptor.getMethodDescriptor());
        JobDetail jobDetail = JobTriggerFactory.jobDetail(descriptor.group(), descriptor.name(), jobDataMap);
        try {
            scheduler.addJob(jobDetail, true);
        } catch (SchedulerException e) {
            LoggerHelper.error("add job failed.", e);
            throw new NiubiException(e);
        }
        List<JobKey> jobKeyList = jobKeyListMap.get(descriptor.group());
        if (jobKeyList == null) {
            jobKeyList = new ArrayList<JobKey>();
        }
        jobKeyList.add(jobKey);
        jobKeyListMap.put(descriptor.group(), jobKeyList);
        if (!sortedGroupList.contains(descriptor.group())) {
            sortedGroupList.add(descriptor.group());
        }
        groupStatusMap.put(descriptor.group(), ScheduleStatus.SHUTDOWN);
    }

}