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

package com.zuoxiaolong.niubi.job.core.scanner;

import com.zuoxiaolong.niubi.job.core.annotation.Disabled;
import com.zuoxiaolong.niubi.job.core.annotation.Schedule;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.metadata.DefaultMethodMetadata;
import com.zuoxiaolong.niubi.job.core.metadata.MethodMetadata;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认的任务扫描器
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 00:45
 */
public class DefaultJobScanner implements JobScanner {

    public List<MethodMetadata> scan(Context context) {
        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();
        for (String packageName : context.getConfiguration().getPackageNames()) {
            methodMetadataList.addAll(scan(context, packageName));
        }
        return methodMetadataList;
    }

    public List<MethodMetadata> scan(Context context, String packageName) {
        URL url = context.getResource(packageName.replace(".", "/"));
        List<MethodMetadata> methodMetadataList = new ArrayList<MethodMetadata>();
        if (url.getProtocol().toLowerCase().equals("file")) {
            LoggerHelper.info("scan package [" + packageName + "]");
            File file = new File(url.getFile());
            fill(context, packageName.indexOf(".") < 0 ? "" : packageName.substring(0, packageName.lastIndexOf(".")), file, methodMetadataList);
            return methodMetadataList;
        } else {
            LoggerHelper.warn("package [" + packageName + "] is not a file but a " + url.getProtocol() + ".");
            return methodMetadataList;
        }
    }

    public void fill(Context context, String packageName, File file, List<MethodMetadata> methodMetadataList) {
        String fileName = file.getName();
        if (file.isFile() && fileName.endsWith(".class")) {
            String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf("."));
            try {
                Class<?> clazz = context.loadClass(className);
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
                    if (methodDisabled != null) {
                        LoggerHelper.info("skip disabled method [" + className + "." + method.getName() + "]");
                        continue;
                    }
                    if (schedule != null) {
                        Type[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes != null && parameterTypes.length > 0) {
                            LoggerHelper.error("schedule method can't have method parameters [" + className + "." + method.getName() + "]");
                        } else {
                            methodMetadataList.add(new DefaultMethodMetadata(schedule, clazz, method));
                            context.getJobBeanFactory().registerJobBeanClass(className, clazz);
                            context.getJobBeanFactory().registerJobBeanInstance(className, context.initializeBean(clazz));
                            LoggerHelper.info("find schedule method [" + className + "." + method.getName() + "]");
                        }
                    }
                }
            } catch (Exception e) {
                LoggerHelper.warn("scan class [" + className + "] failed, has been ignored.");
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    fill(context, packageName + "." + fileName, child, methodMetadataList);
                }
            }
        }
    }

}
