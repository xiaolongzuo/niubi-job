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

package com.zuoxiaolong.niubi.job.core.job;

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.helper.JobContextHelper;
import com.zuoxiaolong.niubi.job.message.log4j.LoggerHelper;
import com.zuoxiaolong.niubi.job.tools.helper.JsonHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;

/**
 * quartz中Job接口的占位类
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:58
 */
public class StubJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        MethodDescriptor methodDescriptor = JobContextHelper.getJobDescriptor(jobExecutionContext);
        JobParameter jobParameter = JobContextHelper.getJobParameter(jobExecutionContext);
        try {
            LoggerHelper.info("begin execute job : " + methodDescriptor);
            LoggerHelper.info("job parameter : " + JsonHelper.toJson(jobParameter));
            Context context = JobContextHelper.getContext(jobExecutionContext);
            if (methodDescriptor.hasParameter()) {
                methodDescriptor.method().invoke(context.jobBeanFactory().getJobBean(methodDescriptor.clazz()), new Object[]{jobParameter});
            } else {
                methodDescriptor.method().invoke(context.jobBeanFactory().getJobBean(methodDescriptor.clazz()), new Object[]{});
            }
            LoggerHelper.info("execute job success: " + methodDescriptor);
        } catch (IllegalAccessException e) {
            LoggerHelper.info("execute job failed: " + methodDescriptor);
            throw new NiubiException(e);
        } catch (InvocationTargetException e) {
            LoggerHelper.info("execute job failed: " + methodDescriptor);
            throw new NiubiException(e);
        }
    }

}
