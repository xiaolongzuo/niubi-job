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

package com.zuoxiaolong.niubi.job.scheduler;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.scanner.job.JobParameter;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:51
 */
public abstract class JobDataMapManager {

    public static SchedulerJobDescriptor getJobDescriptor(JobDetail jobDetail) {
        return (SchedulerJobDescriptor) jobDetail.getJobDataMap().get(SchedulerJobDescriptor.DATA_MAP_KEY);
    }

    public static SchedulerJobDescriptor getJobDescriptor(JobExecutionContext jobExecutionContext) {
        return (SchedulerJobDescriptor) jobExecutionContext.getMergedJobDataMap().get(SchedulerJobDescriptor.DATA_MAP_KEY);
    }

    public static JobParameter getJobParameter(JobExecutionContext jobExecutionContext) {
        return (JobParameter) jobExecutionContext.getMergedJobDataMap().get(JobParameter.DATA_MAP_KEY);
    }

    public static JobBeanFactory getJobBeanFactory(JobExecutionContext jobExecutionContext) {
        try {
            return (JobBeanFactory) jobExecutionContext.getScheduler().getContext().get(JobBeanFactory.DATA_MAP_KEY);
        } catch (SchedulerException e) {
            throw new NiubiException(e);
        }
    }

}
