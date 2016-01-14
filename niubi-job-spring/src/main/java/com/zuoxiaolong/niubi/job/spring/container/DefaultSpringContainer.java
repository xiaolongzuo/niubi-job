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

import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scheduler.schedule.DefaultScheduleManager;
import com.zuoxiaolong.niubi.job.scheduler.schedule.ScheduleManager;
import com.zuoxiaolong.niubi.job.spring.context.DefaultSpringContext;
import com.zuoxiaolong.niubi.job.spring.context.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author 左潇龙
 * @since 1/13/2016 14:22
 */
public class DefaultSpringContainer implements SpringContainer {

    private SpringContext context;

    private ScheduleManager scheduleManager;

    public DefaultSpringContainer(String[] propertiesFileNames) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_XML_PATH);
        this.context = new DefaultSpringContext(applicationContext, propertiesFileNames);
        this.scheduleManager = new DefaultScheduleManager(this.context);
    }

    public DefaultSpringContainer(String jarUrl) {
        this(APPLICATION_CONTEXT_XML_PATH, StringHelper.emptyArray(), new String[]{jarUrl});
    }

    public DefaultSpringContainer(String[] propertiesFileNames, String[] jarUrls) {
        this(APPLICATION_CONTEXT_XML_PATH, propertiesFileNames, jarUrls);
    }

    public DefaultSpringContainer(String applicationContextXmlPath, String[] propertiesFileNames, String[] jarUrls) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(applicationContextXmlPath);
        this.context = new DefaultSpringContext(applicationContext, propertiesFileNames);
        this.scheduleManager = new DefaultScheduleManager(this.context, jarUrls);
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
