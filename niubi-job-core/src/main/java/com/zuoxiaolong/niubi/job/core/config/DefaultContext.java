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

package com.zuoxiaolong.niubi.job.core.config;

import com.zuoxiaolong.niubi.job.core.bean.DefaultJobBeanFactory;
import com.zuoxiaolong.niubi.job.core.bean.JobBeanFactory;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:23
 */
public class DefaultContext implements Context {

    private ClassLoader classLoader;

    private JobBeanFactory jobBeanFactory = new DefaultJobBeanFactory();

    private Configuration configuration;

    public DefaultContext(ClassLoader classLoader, Configuration configuration) {
        this.classLoader = classLoader;
        this.configuration = configuration;
    }

    public DefaultContext(ClassLoader classLoader, Configuration configuration, JobBeanFactory jobBeanFactory) {
        this.classLoader = classLoader;
        this.configuration = configuration;
        this.jobBeanFactory = jobBeanFactory;
    }

    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }

    public JobBeanFactory jobBeanFactory() {
        return jobBeanFactory;
    }

    public Configuration configuration() {
        return configuration;
    }

}
