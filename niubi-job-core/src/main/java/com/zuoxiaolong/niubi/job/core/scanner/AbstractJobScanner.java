package com.zuoxiaolong.niubi.job.core.scanner;

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

import com.zuoxiaolong.niubi.job.core.annotation.Disabled;
import com.zuoxiaolong.niubi.job.core.annotation.Schedule;
import com.zuoxiaolong.niubi.job.core.bean.RegisteredJobBeanFactory;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.job.JobParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author 左潇龙
 * @since 1/12/2016 13:09
 */
public abstract class AbstractJobScanner implements JobScanner {

    private Context context;

    public AbstractJobScanner(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    protected void scanClass(String className, List<MethodTriggerDescriptor> descriptorList) {
        try {
            Class<?> clazz = context.classLoader().loadClass(className);
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
                    descriptorList.add(new MethodTriggerDescriptor(schedule, method, clazz, true));
                    if (context.jobBeanFactory() instanceof RegisteredJobBeanFactory) {
                        ((RegisteredJobBeanFactory)context.jobBeanFactory()).registerJobBeanInstance(clazz);
                    }
                    LoggerHelper.info("find schedule method [" + className + "." + method.getName() + "(JobParameter)]");
                } else if (parameterTypes == null || parameterTypes.length == 0){
                    descriptorList.add(new MethodTriggerDescriptor(schedule, method, clazz, false));
                    if (context.jobBeanFactory() instanceof RegisteredJobBeanFactory) {
                        ((RegisteredJobBeanFactory)context.jobBeanFactory()).registerJobBeanInstance(clazz);
                    }
                    LoggerHelper.info("find schedule method [" + className + "." + method.getName() + "]");
                } else {
                    LoggerHelper.error("schedule method must not have parameter or have a JobParameter parameter [" + className + "." + method.getName() + "]");
                }
            }
        } catch (Exception e) {
            LoggerHelper.warn("scan class [" + className + "] failed, has been ignored.");
        }
    }

}
