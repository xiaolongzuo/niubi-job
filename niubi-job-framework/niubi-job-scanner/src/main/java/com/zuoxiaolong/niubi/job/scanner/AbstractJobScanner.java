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

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.annotation.Disabled;
import com.zuoxiaolong.niubi.job.scanner.annotation.Schedule;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptorFactory;
import com.zuoxiaolong.niubi.job.scanner.job.JobParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抽象的任务扫描器.
 * 该扫描器只会将被{@link Schedule}注解的方法,其它的将会被忽略.
 * 如果被{@link Disabled}注解的类和方法,也将会被忽略.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 *
 * @see Schedule
 * @see Disabled
 *
 */
public abstract class AbstractJobScanner implements JobScanner {

    private List<JobDescriptor> jobDescriptorList;

    private boolean hasSpringEnvironment;

    private ClassLoader classLoader;

    private String[] jarFilePaths;

    private List<String> packagesToScan;

    public AbstractJobScanner(ClassLoader classLoader, String packagesToScan, String... jarFilePaths) {
        this.classLoader = classLoader;
        this.packagesToScan = StringHelper.splitToList(packagesToScan);
        this.hasSpringEnvironment = false;
        this.jarFilePaths = StringHelper.checkEmpty(jarFilePaths);
        this.jobDescriptorList = new ArrayList<>();
    }

    @Override
    public List<JobDescriptor> getJobDescriptorList() {
        return jobDescriptorList;
    }

    @Override
    public boolean hasSpringEnvironment() {
        return hasSpringEnvironment;
    }

    protected void setHasSpringEnvironment(boolean hasSpringEnvironment) {
        this.hasSpringEnvironment = hasSpringEnvironment;
    }

    protected String[] getJarFilePaths() {
        return jarFilePaths;
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    protected List<String> getPackagesToScan() {
        return Collections.unmodifiableList(packagesToScan);
    }

    protected void scanClass(String className) {
        try {
            if (!packagesToScan.isEmpty()) {
                String packageName = ClassHelper.getPackageName(className);
                boolean skipPackage = true;
                for (String packageToScan: packagesToScan) {
                    if (packageName.startsWith(packageToScan)) {
                        skipPackage = false;
                        break;
                    }
                }
                if (skipPackage) {
                    LoggerHelper.debug("skip un-need ro scanned class [" + className + "]");
                    return;
                }
            }
            Class<?> clazz = classLoader.loadClass(className);
            Disabled classDisabled = clazz.getDeclaredAnnotation(Disabled.class);
            if (classDisabled != null) {
                LoggerHelper.debug("skip disabled class [" + className + "]");
                return;
            }
            Method[] methods = clazz.getDeclaredMethods();
            LoggerHelper.debug("scan class [" + className + "]");
            for (Method method : methods) {
                scanMethod(clazz, method);
            }
        } catch (Throwable e) {
            LoggerHelper.debug("scan class [" + className + " : " + e.getClass().getName() + "] failed, has been ignored.", e);
        }
    }

    private void scanMethod(Class<?> clazz, Method method) {
        Schedule schedule = method.getDeclaredAnnotation(Schedule.class);
        Disabled methodDisabled = method.getDeclaredAnnotation(Disabled.class);
        if (methodDisabled != null || schedule == null) {
            LoggerHelper.debug("skip disabled or un-scheduled method [" + clazz.getName() + "." + method.getName() + "]");
            return;
        }
        Type[] parameterTypes = method.getParameterTypes();
        if (parameterTypes != null && parameterTypes.length == 1 && parameterTypes[0] == JobParameter.class) {
            JobDescriptor jobDescriptor = JobDescriptorFactory.jobDescriptor(clazz, method, true, schedule);
            jobDescriptorList.add(jobDescriptor);
            postFindHasParameterJobDescriptor(jobDescriptor);
            LoggerHelper.info("find schedule method [" + clazz.getName() + "." + method.getName() + "(JobParameter)]");
        } else if (parameterTypes == null || parameterTypes.length == 0){
            JobDescriptor jobDescriptor = JobDescriptorFactory.jobDescriptor(clazz, method, false, schedule);
            jobDescriptorList.add(jobDescriptor);
            postFindNotHasParameterJobDescriptor(jobDescriptor);
            LoggerHelper.info("find schedule method [" + clazz.getName() + "." + method.getName() + "]");
        } else {
            LoggerHelper.error("schedule method must not have parameter or have a JobParameter parameter [" + clazz.getName() + "." + method.getName() + "]");
        }
    }

    protected void postFindHasParameterJobDescriptor(JobDescriptor jobDescriptor) {}

    protected void postFindNotHasParameterJobDescriptor(JobDescriptor jobDescriptor) {}

}
