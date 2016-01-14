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
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.message.Factory;
import com.zuoxiaolong.niubi.job.message.Producer;
import com.zuoxiaolong.niubi.job.message.log4j.Log4jMessage;
import com.zuoxiaolong.niubi.job.scheduler.config.Context;
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
        MethodDescriptor methodDescriptor = JobDataMapManager.getJobDescriptor(jobExecutionContext);
        JobParameter jobParameter = JobDataMapManager.getJobParameter(jobExecutionContext);
        Context context = JobDataMapManager.getContext(jobExecutionContext);
        Factory factory = context.jobBeanFactory().getJobBean(Factory.class);
        Producer<Log4jMessage> producer = factory.createProducer();
        String jobMessageString = JsonHelper.toJson(methodDescriptor) + "  " + JsonHelper.toJson(jobParameter);
        try {
            String message = "begin execute job : " + jobMessageString;
            producer.sendMessage(factory.createMessage(Log4jMessage.build(methodDescriptor.clazz(), message)));
            if (methodDescriptor.hasParameter()) {
                methodDescriptor.method().invoke(context.jobBeanFactory().getJobBean(methodDescriptor.clazz()), new Object[]{jobParameter});
            } else {
                methodDescriptor.method().invoke(context.jobBeanFactory().getJobBean(methodDescriptor.clazz()), new Object[]{});
            }
            message = "begin execute job : " + jobMessageString;
            producer.sendMessage(factory.createMessage(Log4jMessage.build(methodDescriptor.clazz(), message)));
        } catch (Exception e) {
            String message = "execute job failed: " + jobMessageString;
            producer.sendMessage(factory.createMessage(Log4jMessage.build(methodDescriptor.clazz(), message, e)));
            throw new NiubiException(e);
        }

    }

}
