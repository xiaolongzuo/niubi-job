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

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scanner.job.JobParameter;
import com.zuoxiaolong.niubi.job.scheduler.context.Context;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * quartz中Job接口的占位类
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:58
 */
public class StubJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDescriptor jobDescriptor = JobDataMapManager.getJobDescriptor(jobExecutionContext);
        JobParameter jobParameter = JobDataMapManager.getJobParameter(jobExecutionContext);
        Context context = JobDataMapManager.getContext(jobExecutionContext);
        String jobMessageString = JsonHelper.toJson(jobDescriptor) + "  " + JsonHelper.toJson(jobParameter);
        try {
            LoggerHelper.info("begin execute job : " + jobMessageString);
            if (jobDescriptor.hasParameter()) {
                jobDescriptor.method().invoke(context.jobBeanFactory().getJobBean(jobDescriptor.clazz()), new Object[]{jobParameter});
            } else {
                jobDescriptor.method().invoke(context.jobBeanFactory().getJobBean(jobDescriptor.clazz()), new Object[]{});
            }
            LoggerHelper.info("begin execute job : " + jobMessageString);
        } catch (Exception e) {
            LoggerHelper.error("execute job failed: " + jobMessageString, e);
            throw new NiubiException(e);
        }

    }

}
