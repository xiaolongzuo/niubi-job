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
 * @author Xiaolong Zuo
 * @since 16/1/9 15:41
 */
public class DefaultJobBeanFactory implements JobBeanFactory {

    private Map<Class<?>, Object> jobBeanInstanceClassMap = new HashMap<>();

    public <T> T getJobBean(Class<T> clazz) {
        T instance = (T) jobBeanInstanceClassMap.get(clazz);
        if (instance != null) {
            return instance;
        }
        return registerJobBeanInstance(clazz);
    }

    private synchronized <T> T registerJobBeanInstance(Class<T> clazz) {
        try {
            T instance = (T) jobBeanInstanceClassMap.get(clazz);
            if (instance == null) {
                instance = clazz.newInstance();
                jobBeanInstanceClassMap.put(clazz, instance);
            }
            return instance;
        } catch (InstantiationException e) {
            throw new NiubiException(e);
        } catch (IllegalAccessException e) {
            throw new NiubiException(e);
        }
    }

}
