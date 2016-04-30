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

package com.zuoxiaolong.niubi.job.scanner.job;

import com.zuoxiaolong.niubi.job.scanner.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scanner.annotation.Schedule;

import java.lang.reflect.Method;

/**
 * 任务描述符工厂,用于创建任务描述符.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public final class JobDescriptorFactory {

    private JobDescriptorFactory() {}

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule) {
        return new DefaultJobDescriptor(clazz, method, hasParameter, schedule);
    }

    public static JobDescriptor jobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy) {
        return new DefaultJobDescriptor(clazz, method, hasParameter, cron, misfirePolicy);
    }

}
