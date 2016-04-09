package com.zuoxiaolong.niubi.job.scanner.job;

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

import com.zuoxiaolong.niubi.job.scanner.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scanner.annotation.Schedule;

import java.lang.reflect.Method;

/**
 * 默认的任务描述符实现
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class DefaultJobDescriptor implements JobDescriptor {

    private String group;

    private String name;

    private Method method;

    private Class<?> clazz;

    private boolean hasParameter;

    protected String cron;

    protected MisfirePolicy misfirePolicy;

    public DefaultJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule) {
        this(clazz, method, hasParameter, schedule.cron(), schedule.misfirePolicy());
    }

    public DefaultJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy) {
        this.group = clazz.getName();
        this.name = method.getName();
        this.clazz = clazz;
        this.method = method;
        this.hasParameter = hasParameter;
        this.cron = cron;
        this.misfirePolicy = misfirePolicy;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public String name() {
        return name;
    }


    @Override
    public Method method() {
        return method;
    }

    @Override
    public boolean hasParameter() {
        return hasParameter;
    }

    @Override
    public Class<?> clazz() {
        return clazz;
    }

    @Override
    public String cron() {
        return cron;
    }

    @Override
    public MisfirePolicy misfirePolicy() {
        return misfirePolicy;
    }

    @Override
    public String toString() {
        return "JobDescriptor:{" +
                "method=" + method +
                ", hasParameter=" + hasParameter +
                ", cron='" + cron + '\'' +
                ", misfirePolicy=" + misfirePolicy +
                '}';
    }

}
