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

package com.zuoxiaolong.niubi.job.scheduler.bean;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认的JobBeanFactory实现,用于非spring环境下加载JobBean的实例
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class DefaultJobBeanFactory implements JobBeanFactory {

    private ClassLoader classLoader;

    public DefaultJobBeanFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private Map<String, Object> jobBeanInstanceClassMap = new HashMap<>();

    @Override
    public <T> T getJobBean(String className) {
        T instance = (T) jobBeanInstanceClassMap.get(className);
        if (instance != null) {
            return instance;
        }
        return registerJobBeanInstance(className);
    }

    private synchronized <T> T registerJobBeanInstance(String className) {
        try {
            T instance = (T) jobBeanInstanceClassMap.get(className);
            if (instance == null) {
                Class<T> clazz = (Class<T>) classLoader.loadClass(className);
                instance = clazz.newInstance();
                jobBeanInstanceClassMap.put(className, instance);
            }
            return instance;
        } catch (Exception e) {
            throw new NiubiException(e);
        }
    }

}
