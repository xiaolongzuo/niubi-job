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

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.scheduler.context.Context;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:51
 */
public abstract class JobDataMapManager {

    public static JobDescriptor getJobDescriptor(JobDetail jobDetail) {
        return (JobDescriptor) jobDetail.getJobDataMap().get(JobDescriptor.DATA_MAP_KEY);
    }

    public static JobDescriptor getJobDescriptor(JobExecutionContext jobExecutionContext) {
        return (JobDescriptor) jobExecutionContext.getMergedJobDataMap().get(JobDescriptor.DATA_MAP_KEY);
    }

    public static JobParameter getJobParameter(JobExecutionContext jobExecutionContext) {
        return (JobParameter) jobExecutionContext.getMergedJobDataMap().get(JobParameter.DATA_MAP_KEY);
    }

    public static Context getContext(JobExecutionContext jobExecutionContext) {
        try {
            return (Context) jobExecutionContext.getScheduler().getContext().get(Context.DATA_MAP_KEY);
        } catch (SchedulerException e) {
            throw new NiubiException(e);
        }
    }

}
