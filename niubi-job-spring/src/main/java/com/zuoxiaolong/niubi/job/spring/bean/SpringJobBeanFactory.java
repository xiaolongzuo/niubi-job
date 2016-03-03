package com.zuoxiaolong.niubi.job.spring.bean;

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

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class SpringJobBeanFactory implements JobBeanFactory {

    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    private Map<String, Object> jobBeanInstanceClassMap = new HashMap<>();

    /**
     * for remote
     * @throws BeansException
     */
    public SpringJobBeanFactory(ClassLoader classLoader) throws BeansException {
        ClassUtils.overrideThreadContextClassLoader(classLoader);
        this.classLoader = classLoader;
        this.applicationContext = new ClassPathXmlApplicationContext(JobScanner.APPLICATION_CONTEXT_XML_PATH);
    }

    /**
     * for local
     * @param applicationContext
     */
    public SpringJobBeanFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getJobBean(String group, String name) {
        String fullClassName = ClassHelper.getFullClassName(group, name);
        Class<T> clazz;
        try {
            clazz = (Class<T>) classLoader.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
            throw new NiubiException(e);
        }
        T instance;
        try {
            instance = applicationContext.getBean(clazz);
        } catch (Throwable e) {
            LoggerHelper.warn("can't find instance for " + fullClassName);
            try {
                instance = registerJobBeanInstance(group, name);
            } catch (Throwable e1) {
                throw new NiubiException(e1);
            }
        }
        return instance;
    }

    private synchronized <T> T registerJobBeanInstance(String group, String name) {
        try {
            String fullClassName = ClassHelper.getFullClassName(group, name);
            T instance = (T) jobBeanInstanceClassMap.get(fullClassName);
            if (instance == null) {
                Class<T> clazz = (Class<T>) classLoader.loadClass(fullClassName);
                instance = clazz.newInstance();
                jobBeanInstanceClassMap.put(fullClassName, instance);
            }
            return instance;
        } catch (InstantiationException e) {
            throw new NiubiException(e);
        } catch (IllegalAccessException e) {
            throw new NiubiException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
