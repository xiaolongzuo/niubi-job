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
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scanner.job.JobParameter;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.quartz.*;

/**
 * 占位任务,它代表着一个由调度器添加的任务,该类会根据调度器传递的参数启动一个任务.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StubJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        Scheduler scheduler = jobExecutionContext.getScheduler();
        JobDescriptor jobDescriptor = JobDataMapManager.getJobDescriptor(jobDetail);
        JobParameter jobParameter = JobDataMapManager.getJobParameter(jobDetail);
        JobBeanFactory jobBeanFactory = getJobBeanFactory(scheduler, jobDetail);
        String jobMessageString = jobDescriptor + "  JobParameter:" + JsonHelper.toJson(jobParameter);
        try {
            LoggerHelper.info("begin execute job : " + jobMessageString);
            if (jobDescriptor.hasParameter()) {
                jobDescriptor.method().invoke(jobBeanFactory.getJobBean(jobDescriptor.group()), new Object[]{jobParameter});
            } else {
                jobDescriptor.method().invoke(jobBeanFactory.getJobBean(jobDescriptor.group()), new Object[]{});
            }
            LoggerHelper.info("execute job success: " + jobMessageString);
        } catch (Exception e) {
            LoggerHelper.error("execute job failed: " + jobMessageString, e);
            throw new NiubiException(e);
        }
    }

    private JobBeanFactory getJobBeanFactory(Scheduler scheduler, JobDetail jobDetail) {
        ScheduleMode scheduleMode = JobDataMapManager.getScheduleMode(scheduler);
        JobBeanFactory jobBeanFactory;
        if (scheduleMode == ScheduleMode.AUTOMATIC) {
            jobBeanFactory = JobDataMapManager.getJobBeanFactory(scheduler);
        } else if (scheduleMode == ScheduleMode.MANUAL) {
            String jarFilePath = JobDataMapManager.getJarFilePath(jobDetail);
            jobBeanFactory = JobEnvironmentCache.instance().getJobBeanFactory(jarFilePath);
        } else {
            throw new NiubiException(new RuntimeException("Unknown schedule mode."));
        }
        return jobBeanFactory;
    }

}
