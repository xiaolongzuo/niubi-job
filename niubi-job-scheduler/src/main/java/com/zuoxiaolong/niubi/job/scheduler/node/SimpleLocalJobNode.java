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

package com.zuoxiaolong.niubi.job.scheduler.node;

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scanner.JobScannerFactory;
import com.zuoxiaolong.niubi.job.scheduler.AutomaticScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.DefaultAutomaticScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.bean.DefaultJobBeanFactory;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;

/**
 * 该实现类用于非集群环境下的非spring任务执行.
 * 内部包含的自动调度器可以按照注解自动的启动的任务.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 *
 * @see AutomaticScheduleManager
 * @see DefaultAutomaticScheduleManager
 *
 */
public class SimpleLocalJobNode extends AbstractNode {

    private AutomaticScheduleManager schedulerManager;

    public SimpleLocalJobNode(String packagesToScan) {
        JobBeanFactory jobBeanFactory = new DefaultJobBeanFactory(ClassHelper.getDefaultClassLoader());
        JobScanner jobScanner = JobScannerFactory.createClasspathJobScanner(ClassHelper.getDefaultClassLoader(), packagesToScan);
        schedulerManager = new DefaultAutomaticScheduleManager(jobBeanFactory, jobScanner.getJobDescriptorList());
    }

    @Override
    public void join() {
        schedulerManager.startup();
    }

    @Override
    public void exit() {
        schedulerManager.shutdown();
    }

}
