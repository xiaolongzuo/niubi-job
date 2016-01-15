package com.zuoxiaolong.niubi.job.scanner;

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

import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.annotation.Disabled;
import com.zuoxiaolong.niubi.job.scanner.annotation.Schedule;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptorFactory;
import com.zuoxiaolong.niubi.job.scanner.job.JobParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author 左潇龙
 * @since 1/12/2016 13:09
 */
public abstract class AbstractJobScanner implements JobScanner {

    protected JobScanClassLoader classLoader;

    public AbstractJobScanner(JobScanClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected void scanClass(String className, List<JobDescriptor> descriptorList) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            Disabled classDisabled = clazz.getDeclaredAnnotation(Disabled.class);
            if (classDisabled != null) {
                LoggerHelper.info("skip disabled class [" + className + "]");
                return;
            }
            Method[] methods = clazz.getDeclaredMethods();
            LoggerHelper.info("scan class [" + className + "]");
            for (Method method : methods) {
                Schedule schedule = method.getDeclaredAnnotation(Schedule.class);
                Disabled methodDisabled = method.getDeclaredAnnotation(Disabled.class);
                if (methodDisabled != null || schedule == null) {
                    LoggerHelper.info("skip disabled or un-scheduled method [" + className + "." + method.getName() + "]");
                    continue;
                }
                Type[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length == 1 && parameterTypes[0] == JobParameter.class) {
                    JobDescriptor jobDescriptor = JobDescriptorFactory.jobDescriptor(clazz, method, true, schedule);
                    descriptorList.add(jobDescriptor);
                    postFindHasParameterJobDescriptor(jobDescriptor);
                    LoggerHelper.info("find schedule method [" + className + "." + method.getName() + "(JobParameter)]");
                } else if (parameterTypes == null || parameterTypes.length == 0){
                    JobDescriptor jobDescriptor = JobDescriptorFactory.jobDescriptor(clazz, method, false, schedule);
                    descriptorList.add(jobDescriptor);
                    postFindNotHasParameterJobDescriptor(jobDescriptor);
                    LoggerHelper.info("find schedule method [" + className + "." + method.getName() + "]");
                } else {
                    LoggerHelper.error("schedule method must not have parameter or have a JobParameter parameter [" + className + "." + method.getName() + "]");
                }
            }
        } catch (Exception e) {
            LoggerHelper.warn("scan class [" + className + " : " + e.getClass().getName() + "] failed, has been ignored.");
        }
    }

    protected void postFindHasParameterJobDescriptor(JobDescriptor jobDescriptor) {}

    protected void postFindNotHasParameterJobDescriptor(JobDescriptor jobDescriptor) {}

}
