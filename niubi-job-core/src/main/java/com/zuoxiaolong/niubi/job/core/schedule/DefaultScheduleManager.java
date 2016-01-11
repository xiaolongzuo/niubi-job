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

package com.zuoxiaolong.niubi.job.core.schedule;

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.config.Configuration;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.helper.JobContextHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.metadata.MethodMetadata;
import com.zuoxiaolong.niubi.job.core.metadata.PlaceholderJob;
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

    public DefaultScheduleManager(Configuration configuration) {
        this.jobKeyListMap = new ConcurrentHashMap<String, List<JobKey>>();
        this.sortedGroupList = new ArrayList<String>();
        this.groupStatusMap = new ConcurrentHashMap<String, ScheduleStatus>();
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            schedulerFactory.initialize(configuration.getProperties());
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            LoggerHelper.error("create scheduler failed.", e);
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
                MethodMetadata methodMetadata = JobContextHelper.getMethodMetadata(jobDetail);
                ScheduleBuilder scheduleBuilder = methodMetadata.schedule().scheduleType().scheduleBuilder(methodMetadata.schedule());
                scheduler.scheduleJob(TriggerBuilder.newTrigger().forJob(jobDetail).withSchedule(scheduleBuilder).build());
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

    public void addJob(MethodMetadata methodMetadata) {
        String name = methodMetadata.clazz().getName() + "." + methodMetadata.method().getName();
        String group = methodMetadata.schedule().group();
        JobKey jobKey = new JobKey(name, group);
        JobDetail jobDetail = createJob(jobKey, methodMetadata);
        try {
            scheduler.addJob(jobDetail, true);
        } catch (SchedulerException e) {
            LoggerHelper.error("add job failed.", e);
            throw new NiubiException(e);
        }
        List<JobKey> jobKeyList = jobKeyListMap.get(group);
        if (jobKeyList == null) {
            jobKeyList = new ArrayList<JobKey>();
        }
        jobKeyList.add(jobKey);
        jobKeyListMap.put(group, jobKeyList);
        if (!sortedGroupList.contains(group)) {
            sortedGroupList.add(group);
        }
        groupStatusMap.put(group, ScheduleStatus.SHUTDOWN);
    }

    protected JobDetail createJob(JobKey jobKey, MethodMetadata methodMetadata) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(MethodMetadata.DATA_MAP_KEY, methodMetadata);
        JobDetail jobDetail = JobBuilder
                .newJob(PlaceholderJob.class)
                .withIdentity(jobKey)
                .setJobData(jobDataMap)
                .storeDurably(true)
                .build();
        return jobDetail;
    }

}