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

import java.lang.reflect.Method;

/**
 * 触发器工厂
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:10
 */
public class JobDescriptorFactory {

    private JobDescriptorFactory() {}

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule) {
        return jobDescriptor(clazz, method, hasParameter, schedule, new JobDataMap());
    }

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule, JobDataMap jobDataMap) {
        return new DefaultJobDescriptor(clazz, method, hasParameter, schedule, jobDataMap);
    }

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy) {
        return jobDescriptor(clazz, method, hasParameter, cron, misfirePolicy, new JobDataMap());
    }

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy, JobDataMap jobDataMap) {
        return new DefaultJobDescriptor(clazz, method, hasParameter, cron, misfirePolicy, jobDataMap);
    }

}
