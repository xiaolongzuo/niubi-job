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


package com.zuoxiaolong.niubi.job.scheduler;

import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.JobScannerFactory;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4
 */
public class JobJarCache {

    private static JobJarCache jobJarCache = new JobJarCache();

    private Map<String, JobBeanFactory> jobBeanFactoryCache = new HashMap<>();

    private Map<String, Map<String, SchedulerJobDescriptor>> JobDescriptorCache = new HashMap<>();

    private JobJarCache() {}

    public static JobJarCache instance() {
        return jobJarCache;
    }

    public JobBeanFactory getJobBeanFactory(String jarFilePath, boolean isSpring) throws Exception {
        JobBeanFactory jobBeanFactory = jobBeanFactoryCache.get(jarFilePath);
        if (jobBeanFactory != null) {
            return jobBeanFactory;
        }
        synchronized (jobBeanFactoryCache) {
            //double check
            jobBeanFactory = jobBeanFactoryCache.get(jarFilePath);
            if (jobBeanFactory != null) {
                return jobBeanFactory;
            }
            jobBeanFactory = createJobBeanFactory(jarFilePath, isSpring);
            AssertHelper.notNull(jobBeanFactory, "jobBeanFactory create failed.");
            jobBeanFactoryCache.put(jarFilePath, jobBeanFactory);
            return jobBeanFactory;
        }
    }

    private JobBeanFactory createJobBeanFactory(String jarFilePath, boolean isSpring) throws Exception {
        String jobBeanFactoryClassName;
        if (isSpring) {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory";
        } else {
            jobBeanFactoryClassName = "com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory";
        }
        ClassLoader jarApplicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        Class<? extends JobBeanFactory> jobBeanFactoryClass = (Class<? extends JobBeanFactory>) jarApplicationClassLoader.loadClass(jobBeanFactoryClassName);
        Class<?>[] parameterTypes = new Class[]{ClassLoader.class};
        Constructor<? extends JobBeanFactory> jobBeanFactoryConstructor = jobBeanFactoryClass.getConstructor(parameterTypes);
        return jobBeanFactoryConstructor.newInstance(jarApplicationClassLoader);
    }

    public SchedulerJobDescriptor getJobDescriptor(String jarFilePath, String packagesToScan, String group, String name) {
        String uniqueDescriptor = ClassHelper.getUniqueDescriptor(group, name);
        Map<String, SchedulerJobDescriptor> jobDescriptorMap = JobDescriptorCache.get(jarFilePath);
        if (jobDescriptorMap != null) {
            return jobDescriptorMap.get(uniqueDescriptor);
        }
        synchronized (JobDescriptorCache) {
            //double check
            jobDescriptorMap = JobDescriptorCache.get(jarFilePath);
            if (jobDescriptorMap != null) {
                return jobDescriptorMap.get(uniqueDescriptor);
            }
            jobDescriptorMap = createJobDescriptorMap(jarFilePath, packagesToScan, group, name);
            AssertHelper.notNull(jobDescriptorMap, "jobDescriptorMap create failed.");
            JobDescriptorCache.put(jarFilePath, jobDescriptorMap);
            return jobDescriptorMap.get(uniqueDescriptor);
        }
    }

    private Map<String, SchedulerJobDescriptor> createJobDescriptorMap(String jarFilePath, String packagesToScan, String group, String name) {
        ClassLoader jarApplicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        JobScanner jobScanner = JobScannerFactory.createJarFileJobScanner(jarApplicationClassLoader, packagesToScan, jarFilePath);
        List<JobDescriptor> jobDescriptorList = jobScanner.getJobDescriptorList();
        Map<String, SchedulerJobDescriptor> jobDescriptorMap = new HashMap<>();
        if (jobDescriptorList != null) {
            for (JobDescriptor jobDescriptor : jobDescriptorList) {
                jobDescriptorMap.put(ClassHelper.getUniqueDescriptor(group, name), new DefaultSchedulerJobDescriptor(jobDescriptor));
            }
        }
        return jobDescriptorMap;
    }

}
