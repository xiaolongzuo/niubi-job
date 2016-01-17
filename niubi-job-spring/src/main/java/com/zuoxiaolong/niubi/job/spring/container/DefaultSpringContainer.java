package com.zuoxiaolong.niubi.job.spring.container;

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
import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanClassLoader;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.config.Configuration;
import com.zuoxiaolong.niubi.job.scheduler.schedule.DefaultScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.schedule.ScheduleManager;
import com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * @author 左潇龙
 * @since 1/13/2016 14:22
 */
public class DefaultSpringContainer implements SpringContainer {

    private JobScanClassLoader classLoader;

    private JobBeanFactory jobBeanFactory;

    private ApplicationContext applicationContext;

    private ScheduleManager scheduleManager;

    public DefaultSpringContainer(Configuration configuration, String packagesToScan) {
        this.classLoader = new JobScanClassLoader(ClassHelper.getDefaultClassLoader());
        ClassUtils.overrideThreadContextClassLoader(this.classLoader);
        this.applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.jobBeanFactory = new SpringJobBeanFactory(applicationContext);
        this.scheduleManager = new DefaultScheduleManager(this.classLoader, this.jobBeanFactory, configuration, packagesToScan);
    }

    public DefaultSpringContainer(Configuration configuration, String packagesToScan, String jarUrl) {
        this(configuration, packagesToScan, new String[]{jarUrl});
    }

    public DefaultSpringContainer(Configuration configuration, String packagesToScan, String[] jarUrls) {
        this.classLoader = new JobScanClassLoader(ClassHelper.getDefaultClassLoader());
        String[] jarFilePaths = StringHelper.emptyArray();
        if (!ListHelper.isEmpty(jarUrls)) {
            jarFilePaths = JarFileHelper.download(this.classLoader.getResource("").getFile(), jarUrls);
            this.classLoader.addJarFiles(jarFilePaths);
        }
        ClassUtils.overrideThreadContextClassLoader(this.classLoader);
        this.applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.jobBeanFactory = new SpringJobBeanFactory(applicationContext);
        this.scheduleManager = new DefaultScheduleManager(this.classLoader, this.jobBeanFactory, configuration, packagesToScan, jarFilePaths);
    }

    @Override
    public ApplicationContext applicationContext() {
        return applicationContext;
    }

    @Override
    public JobScanClassLoader classLoader() {
        return classLoader;
    }

    @Override
    public JobBeanFactory jobBeanFactory() {
        return jobBeanFactory;
    }

    @Override
    public ScheduleManager scheduleManager() {
        return scheduleManager;
    }

}
