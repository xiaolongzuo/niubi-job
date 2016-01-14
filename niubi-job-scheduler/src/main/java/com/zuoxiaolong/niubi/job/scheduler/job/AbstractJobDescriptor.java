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
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.lang.reflect.Method;

/**
 * @author Xiaolong Zuo
 * @since 1/12/2016 17:29
 */
public abstract class AbstractJobDescriptor extends AbstractKeyDescriptor implements JobDescriptor {

    private Method method;

    private Class<?> clazz;

    private boolean hasParameter;

    private String cron;

    private MisfirePolicy misfirePolicy;

    public AbstractJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, Schedule schedule) {
        this(clazz, method, hasParameter, schedule.cron(), schedule.misfirePolicy());
    }

    public AbstractJobDescriptor(Class<?> clazz, Method method, boolean hasParameter, String cron, MisfirePolicy misfirePolicy) {
        super(clazz.getName(), method.getName());
        this.clazz = clazz;
        this.method = method;
        this.hasParameter = hasParameter;
        this.cron = cron;
        this.misfirePolicy = misfirePolicy;
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

    public Trigger trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(name(), group())
                .withIdentity(name(), group())
                .withSchedule(scheduleBuilder())
                .build();
    }

    @Override
    public boolean isManualTrigger() {
        return cron == null || misfirePolicy == null;
    }

    @Override
    public JobDescriptor withTrigger(String cron, MisfirePolicy misfirePolicy) {
        this.cron = cron;
        this.misfirePolicy = misfirePolicy;
        return this;
    }

    protected abstract ScheduleBuilder scheduleBuilder();

    @Override
    public String toString() {
        return "AbstractJobDescriptor{" +
                "method=" + method +
                ", clazz=" + clazz +
                ", hasParameter=" + hasParameter +
                ", cron='" + cron + '\'' +
                ", misfirePolicy=" + misfirePolicy +
                '}';
    }

}
