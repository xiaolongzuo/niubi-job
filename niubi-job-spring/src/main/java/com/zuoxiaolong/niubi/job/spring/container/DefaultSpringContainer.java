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

import com.zuoxiaolong.niubi.job.scheduler.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scheduler.scanner.LocalJobScanner;
import com.zuoxiaolong.niubi.job.scheduler.scanner.MethodTriggerDescriptor;
import com.zuoxiaolong.niubi.job.scheduler.scanner.RemoteJobScanner;
import com.zuoxiaolong.niubi.job.scheduler.schedule.DefaultScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.schedule.ScheduleManager;
import com.zuoxiaolong.niubi.job.spring.context.DefaultSpringContext;
import com.zuoxiaolong.niubi.job.spring.context.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author 左潇龙
 * @since 1/13/2016 14:22
 */
public class DefaultSpringContainer implements SpringContainer {

    private JobScanner jobScanner;

    private SpringContext context;

    private ScheduleManager scheduleManager;

    public DefaultSpringContainer() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML_PATH);
        this.context = new DefaultSpringContext(applicationContext);
        this.jobScanner = new LocalJobScanner(context);
        createScheduleManager();
    }

    public DefaultSpringContainer(String jarUrl) {
        this(APPLICATION_CONTEXT_XML_PATH, jarUrl);
    }

    public DefaultSpringContainer(String applicationContextXmlPath, String jarUrl) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(applicationContextXmlPath);
        this.context = new DefaultSpringContext(applicationContext);
        this.jobScanner = new RemoteJobScanner(context, jarUrl);
        createScheduleManager();
    }

    private void createScheduleManager() {
        scheduleManager = new DefaultScheduleManager(context);
        List<MethodTriggerDescriptor> descriptorList = jobScanner.scan();
        for (MethodTriggerDescriptor descriptor : descriptorList) {
            scheduleManager.addJob(descriptor);
        }
        scheduleManager.bindContext(context);
    }

    @Override
    public SpringContext getContext() {
        return context;
    }

    @Override
    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

}
