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

import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.config.Configuration;
import com.zuoxiaolong.niubi.job.scheduler.container.AbstractContainer;
import com.zuoxiaolong.niubi.job.scheduler.schedule.DefaultScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.schedule.ScheduleManager;
import com.zuoxiaolong.niubi.job.spring.bean.SpringJobBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author 左潇龙
 * @since 1/13/2016 14:22
 */
public class DefaultSpringContainer extends AbstractContainer {

    private JobBeanFactory jobBeanFactory;

    private ScheduleManager scheduleManager;

    /**
     * for local
     * @param applicationContext
     * @param configuration
     * @param packagesToScan
     */
    public DefaultSpringContainer(ApplicationContext applicationContext, Configuration configuration, String packagesToScan) {
        super(applicationContext.getClassLoader(), packagesToScan);
        this.jobBeanFactory = new SpringJobBeanFactory(applicationContext);
        this.scheduleManager = new DefaultScheduleManager(configuration, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    /**
     * for remote
     * @param configuration
     * @param packagesToScan
     * @param jarUrls
     */
    public DefaultSpringContainer(Configuration configuration, String packagesToScan, String... jarUrls) {
        super(packagesToScan, jarUrls);
        this.jobBeanFactory = new SpringJobBeanFactory();
        this.scheduleManager = new DefaultScheduleManager(configuration, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    @Override
    public ScheduleManager scheduleManager() {
        return scheduleManager;
    }

}
