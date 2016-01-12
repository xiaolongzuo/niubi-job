package com.zuoxiaolong.niubi.job.core.scanner;

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

import com.zuoxiaolong.niubi.job.core.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.core.annotation.Schedule;
import com.zuoxiaolong.niubi.job.core.job.JobTriggerFactory;
import com.zuoxiaolong.niubi.job.core.job.MethodDescriptor;
import com.zuoxiaolong.niubi.job.core.job.TriggerDescriptor;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.lang.reflect.Method;

/**
 * @author 左潇龙
 * @since 1/12/2016 18:35
 */
public class MethodTriggerDescriptor {

    private TriggerDescriptor triggerDescriptor;

    private MethodDescriptor methodDescriptor;

    public MethodTriggerDescriptor(Schedule schedule, Method method, Class<?> clazz, boolean hasParameter) {
        this.triggerDescriptor = JobTriggerFactory.triggerDescriptor(schedule);
        this.methodDescriptor = JobTriggerFactory.methodDescriptor(clazz, method, hasParameter);
    }

    public String cron() {
        return triggerDescriptor.cron();
    }

    public MisfirePolicy misfirePolicy() {
        return triggerDescriptor.misfirePolicy();
    }

    public TriggerKey triggerKey() {
        return triggerDescriptor.triggerKey();
    }

    public Trigger trigger() {
        return triggerDescriptor.trigger();
    }

    public JobKey jobKey() {
        return triggerDescriptor.jobKey();
    }

    public String name() {
        return triggerDescriptor.name();
    }

    public String group() {
        return triggerDescriptor.group();
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public TriggerDescriptor getTriggerDescriptor() {
        return triggerDescriptor;
    }

}
