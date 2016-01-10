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

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.bean.DefaultJobBeanFactory;
import com.zuoxiaolong.niubi.job.core.bean.JobBeanFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:23
 */
public class DefaultContext implements Context {

    private ClassLoader classLoader = DefaultContext.class.getClassLoader();

    private JobBeanFactory jobBeanFactory = new DefaultJobBeanFactory();

    private Configuration configuration;

    public DefaultContext(Configuration configuration) {
        this.configuration = configuration;
    }

    public DefaultContext(ClassLoader classLoader, Configuration configuration) {
        this.classLoader = classLoader;
        this.configuration = configuration;
    }

    public <T> Class<T> loadClass(String className) {
        try {
            return (Class<T>) classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new NiubiException(e);
        }
    }

    public <T> T initializeBean(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new NiubiException(e);
        } catch (IllegalAccessException e) {
            throw new NiubiException(e);
        }
    }

    public URL getResource(String name) {
        return classLoader.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        return classLoader.getResourceAsStream(name);
    }

    public JobBeanFactory getJobBeanFactory() {
        return jobBeanFactory;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
