package com.zuoxiaolong.niubi.job.core.job;

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
import com.zuoxiaolong.niubi.job.tools.StringHelper;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.UUID;

/**
 * @author 左潇龙
 * @since 1/12/2016 16:18
 */
public abstract class AbstractTriggerDescriptor extends AbstractKeyDescriptor implements TriggerDescriptor {

    private String cron;

    private MisfirePolicy misfirePolicy = MisfirePolicy.None;

    public AbstractTriggerDescriptor(Schedule schedule) {
        this(schedule.group(), StringHelper.isEmpty(schedule.name(), UUID.randomUUID().toString()), schedule.cron(), schedule.misfirePolicy());
    }

    public AbstractTriggerDescriptor(String group, String name, String cron, MisfirePolicy misfirePolicy) {
        super(group, name);
        this.cron = cron;
        this.misfirePolicy = misfirePolicy;
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

    protected abstract ScheduleBuilder scheduleBuilder();

}
