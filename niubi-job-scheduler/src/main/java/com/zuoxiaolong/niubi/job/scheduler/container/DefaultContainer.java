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

import com.zuoxiaolong.niubi.job.scheduler.DefaultSchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.SchedulerManager;
import com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;

import java.util.Properties;

/**
 * 默认的容器实现类
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 01:18
 */
public class DefaultContainer extends AbstractContainer {

    private JobBeanFactory jobBeanFactory;

    private SchedulerManager schedulerManager;

    /**
     * for local
     * @param packagesToScan
     */
    public DefaultContainer(String packagesToScan) {
        super(packagesToScan);
        this.jobBeanFactory = new DefaultJobBeanFactory();
        this.schedulerManager = new DefaultSchedulerManager(this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    /**
     * for remote
     * @param classLoader
     * @param packagesToScan
     * @param jarFilePath
     */
    public DefaultContainer(ClassLoader classLoader, Properties properties, String packagesToScan, String jarFilePath) {
        super(classLoader, packagesToScan, jarFilePath);
        this.jobBeanFactory = new DefaultJobBeanFactory();
        this.schedulerManager = new DefaultSchedulerManager(properties, this.jobBeanFactory, getJobScanner().getJobDescriptorList());
    }

    public SchedulerManager schedulerManager() {
        return schedulerManager;
    }

}
