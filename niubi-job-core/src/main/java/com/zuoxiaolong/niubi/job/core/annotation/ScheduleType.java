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

package com.zuoxiaolong.niubi.job.core.annotation;

import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

/**
 * 目前主要支持两种类型的调度方式
 * 1.cron表达式(CRON)
 * 2.interval模式(SIMPLE)
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 04:22
 */
public enum ScheduleType {

    CRON {
        @Override
        public ScheduleBuilder scheduleBuilder(Schedule schedule) {
            if (schedule.misfirePolicy() == MisfirePolicy.IGNORE) {
                return CronScheduleBuilder.cronSchedule(schedule.cron()).withMisfireHandlingInstructionIgnoreMisfires();
            } else {
                return CronScheduleBuilder.cronSchedule(schedule.cron()).withMisfireHandlingInstructionDoNothing();
            }
        }
    }, SIMPLE {
        @Override
        public ScheduleBuilder scheduleBuilder(Schedule schedule) {
            if (schedule.misfirePolicy() == MisfirePolicy.IGNORE) {
                return SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInMilliseconds(schedule.interval())
                        .withRepeatCount(schedule.repeatCount())
                        .withMisfireHandlingInstructionIgnoreMisfires();
            } else {
                return SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInMilliseconds(schedule.interval())
                        .withRepeatCount(schedule.repeatCount())
                        .withMisfireHandlingInstructionNowWithRemainingCount();
            }
        }
    };

    public abstract ScheduleBuilder scheduleBuilder(Schedule schedule);

}
