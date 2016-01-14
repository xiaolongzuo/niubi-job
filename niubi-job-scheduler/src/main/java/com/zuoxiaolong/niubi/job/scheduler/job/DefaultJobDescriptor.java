package com.zuoxiaolong.niubi.job.scheduler.job;

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

import com.zuoxiaolong.niubi.job.scheduler.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scheduler.annotation.Schedule;
import org.quartz.*;

import java.lang.reflect.Method;

/**
 * @author Xiaolong Zuo
 * @since 1/12/2016 16:38
 */
public class DefaultJobDescriptor extends AbstractJobDescriptor {

    private JobDataMap jobDataMap;

    DefaultJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule, JobDataMap jobDataMap) {
        super(clazz, method, hasParameter, schedule);
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
        this.jobDataMap = jobDataMap;
    }

    DefaultJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy, JobDataMap jobDataMap) {
        super(clazz, method, hasParameter, cron, misfirePolicy);
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
        this.jobDataMap = jobDataMap;
    }

    public JobDataMap jobDataMap() {
        return jobDataMap;
    }

    public JobDetail jobDetail() {
        return JobBuilder.newJob(StubJob.class)
                .withIdentity(name(), group())
                .storeDurably(true)
                .setJobData(jobDataMap)
                .build();
    }

    protected ScheduleBuilder scheduleBuilder() {
        if (misfirePolicy() == MisfirePolicy.IgnoreMisfires) {
            return CronScheduleBuilder.cronSchedule(cron()).withMisfireHandlingInstructionIgnoreMisfires();
        } else if (misfirePolicy() == MisfirePolicy.DoNothing) {
            return CronScheduleBuilder.cronSchedule(cron()).withMisfireHandlingInstructionDoNothing();
        } else if (misfirePolicy() == MisfirePolicy.FireAndProceed){
            return CronScheduleBuilder.cronSchedule(cron()).withMisfireHandlingInstructionFireAndProceed();
        } else {
            return CronScheduleBuilder.cronSchedule(cron());
        }
    }

}
