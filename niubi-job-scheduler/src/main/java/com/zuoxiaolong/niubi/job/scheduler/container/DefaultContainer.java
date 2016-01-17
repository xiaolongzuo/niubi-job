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

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanClassLoader;
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
public class DefaultContainer implements Container {

    private JobScanClassLoader classLoader;

    private JobBeanFactory jobBeanFactory;

    private ScheduleManager scheduleManager;

    public DefaultContainer(Configuration configuration, String packagesToScan) {
        this.classLoader = new JobScanClassLoader(ClassHelper.getDefaultClassLoader());
        this.jobBeanFactory = new DefaultJobBeanFactory();
        this.scheduleManager = new DefaultScheduleManager(this.classLoader, this.jobBeanFactory, configuration, packagesToScan);
    }

    public DefaultContainer(Configuration configuration, String packagesToScan, String jarUrl) {
        this(configuration, packagesToScan, new String[]{jarUrl});
    }

    public DefaultContainer(Configuration configuration, String packagesToScan, String[] jarUrls) {
        this.classLoader = new JobScanClassLoader(ClassHelper.getDefaultClassLoader());
        String[] jarFilePaths = StringHelper.emptyArray();
        if (!ListHelper.isEmpty(jarUrls)) {
            jarFilePaths = JarFileHelper.download(this.classLoader.getResource("").getFile(), jarUrls);
            this.classLoader.addJarFiles(jarFilePaths);
        }
        this.jobBeanFactory = new DefaultJobBeanFactory();
        this.scheduleManager = new DefaultScheduleManager(this.classLoader, this.jobBeanFactory, configuration, packagesToScan, jarFilePaths);
    }

    @Override
    public JobScanClassLoader classLoader() {
        return classLoader;
    }

    public JobBeanFactory jobBeanFactory() {
        return jobBeanFactory;
    }

    public ScheduleManager scheduleManager() {
        return scheduleManager;
    }

}
