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

package com.zuoxiaolong.niubi.job.core.bean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 15:41
 */
public class DefaultJobBeanFactory implements JobBeanFactory {

    private ConcurrentHashMap<String, Class<?>> jobBeanClassMap = new ConcurrentHashMap<String, Class<?>>();

    private ConcurrentHashMap<String, Object> jobBeanInstanceMap = new ConcurrentHashMap<String, Object>();

    public <T> void registerJobBeanInstance(String jobClassName, T instance) {
        jobBeanInstanceMap.put(jobClassName, instance);
    }

    public <T> void registerJobBeanClass(String jobClassName, Class<T> clazz) {
        jobBeanClassMap.put(jobClassName, clazz);
    }

    public <T> Class<T> getJobBeanClass(String name) {
        return (Class<T>) jobBeanClassMap.get(name);
    }

    public <T> T getJobBean(String name) {
        return (T) jobBeanInstanceMap.get(name);
    }

    public <T> T getJobBean(Class<T> clazz) {
        return (T) jobBeanInstanceMap.get(clazz.getName());
    }

}
