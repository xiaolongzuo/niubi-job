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

package com.zuoxiaolong.niubi.job.scheduler.job;

import com.zuoxiaolong.niubi.job.scheduler.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scheduler.annotation.Schedule;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;

import java.lang.reflect.Method;

/**
 * 触发器工厂
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:10
 */
public class JobTriggerFactory {

    private JobTriggerFactory() {}

    public static TriggerDescriptor triggerDescriptor(Schedule schedule) {
        return new DefaultTriggerDescriptor(schedule);
    }

    public static Trigger trigger(Schedule schedule) {
        return new DefaultTriggerDescriptor(schedule).trigger();
    }

    public static Trigger trigger(String group, String name, String cron, MisfirePolicy misfirePolicy) {
        return new DefaultTriggerDescriptor(group, name, cron, misfirePolicy).trigger();
    }

    public static MethodDescriptor methodDescriptor(Class<?> clazz, Method method, boolean hasParameter) {
        return new DefaultMethodDescriptor(clazz, method, hasParameter);
    }

    public static JobDetail jobDetail(String group, String name) {
        return new DefaultJobDescriptor(group, name, null).jobDetail();
    }

    public static JobDetail jobDetail(String group, String name, JobDataMap jobDataMap) {
        return new DefaultJobDescriptor(group, name, jobDataMap).jobDetail();
    }

    public static JobDetail jobDetail(JobKey jobKey, JobDataMap jobDataMap) {
        return new DefaultJobDescriptor(jobKey.getGroup(), jobKey.getName(), jobDataMap).jobDetail();
    }

}
