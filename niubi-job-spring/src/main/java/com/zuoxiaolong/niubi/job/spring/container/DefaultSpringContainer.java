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

import com.zuoxiaolong.niubi.job.scheduler.DefaultSchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.SchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.container.AbstractContainer;
import com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

/**
 *
 * @author 左潇龙
 * @since 1/13/2016 14:22
 */
public class DefaultSpringContainer extends AbstractContainer {

    private JobBeanFactory jobBeanFactory;

    private SchedulerManager schedulerManager;

    /**
     * for local
     * @param applicationContext
     * @param packagesToScan
     */
    public DefaultSpringContainer(ApplicationContext applicationContext, String packagesToScan) {
        super(packagesToScan);
        this.jobBeanFactory = new SpringJobBeanFactory(applicationContext);
        this.schedulerManager = new DefaultSchedulerManager(this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    /**
     * for remote
     * @param packagesToScan
     */
    public DefaultSpringContainer(ClassLoader classLoader, Properties properties, String packagesToScan, String jarFilePath) {
        super(classLoader, packagesToScan, jarFilePath);
        this.jobBeanFactory = new SpringJobBeanFactory();
        this.schedulerManager = new DefaultSchedulerManager(properties, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    @Override
    public SchedulerManager schedulerManager() {
        return schedulerManager;
    }

}
