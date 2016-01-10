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

package com.zuoxiaolong.niubi.job.core.metadata;

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.helper.JobExecutionContextHelper;
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
public class PlaceholderJob implements Job {

    private static final Object[] EMPTY_ARGUMENTS = new Object[]{};

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            MethodMetadata methodMetadata = JobExecutionContextHelper.getMethodMetadata(jobExecutionContext);
            Context context = JobExecutionContextHelper.getContext(jobExecutionContext);
            methodMetadata.method().invoke(context.getJobBeanFactory().getJobBean(methodMetadata.clazz()), EMPTY_ARGUMENTS);
        } catch (IllegalAccessException e) {
            throw new NiubiException(e);
        } catch (InvocationTargetException e) {
            throw new NiubiException(e);
        }
    }

}
