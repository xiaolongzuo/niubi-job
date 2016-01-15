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

package com.zuoxiaolong.niubi.job.scheduler.schedule;

import com.zuoxiaolong.niubi.job.scanner.annotation.MisfirePolicy;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * @author Xiaolong Zuo
 * @since 16/1/16 01:30
 */
public interface ScheduleJobDescriptor extends JobDescriptor {

    boolean isManualTrigger();

    JobDetail jobDetail();

    ScheduleJobDescriptor putJobData(String key, Object value);

    Trigger trigger();

    ScheduleJobDescriptor withTrigger(String cron, MisfirePolicy misfirePolicy);

    TriggerKey triggerKey();

    JobKey jobKey();

}
