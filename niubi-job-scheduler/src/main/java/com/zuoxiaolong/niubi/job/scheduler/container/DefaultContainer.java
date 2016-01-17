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

package com.zuoxiaolong.niubi.job.scheduler.container;

import com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.config.Configuration;
import com.zuoxiaolong.niubi.job.scheduler.schedule.DefaultScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.schedule.ScheduleManager;

/**
 * 默认的容器实现类
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:18
 */
public class DefaultContainer extends AbstractContainer {

    private JobBeanFactory jobBeanFactory;

    private ScheduleManager scheduleManager;

    /**
     * for local
     * @param classLoader
     * @param packagesToScan
     */
    public DefaultContainer(ClassLoader classLoader, String packagesToScan) {
        super(classLoader, packagesToScan);
        this.jobBeanFactory = new DefaultJobBeanFactory();
        Configuration configuration = new Configuration(classLoader);
        this.scheduleManager = new DefaultScheduleManager(configuration, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    /**
     * for remote
     * @param configuration
     * @param packagesToScan
     * @param jarUrls
     */
    public DefaultContainer(Configuration configuration, String packagesToScan, String... jarUrls) {
        super(packagesToScan, jarUrls);
        this.jobBeanFactory = new DefaultJobBeanFactory();
        this.scheduleManager = new DefaultScheduleManager(configuration, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }


    public ScheduleManager scheduleManager() {
        return scheduleManager;
    }

}
