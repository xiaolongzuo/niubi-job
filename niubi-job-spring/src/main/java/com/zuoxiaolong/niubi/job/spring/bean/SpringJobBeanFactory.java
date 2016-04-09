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
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanner;
import com.zuoxiaolong.niubi.job.scheduler.bean.JobBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * spring环境下的JobBean工厂实现,所有的JobBean优先从spring IOC容器中获取.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class SpringJobBeanFactory implements JobBeanFactory {

    private ApplicationContext applicationContext;

    private ClassLoader classLoader;

    /**
     * 该构造函数用于集群环境
     *
     * @throws BeansException
     */
    public SpringJobBeanFactory(ClassLoader classLoader) throws BeansException {
        this.classLoader = classLoader;
        ClassUtils.overrideThreadContextClassLoader(classLoader);
        this.applicationContext = new ClassPathXmlApplicationContext(JobScanner.APPLICATION_CONTEXT_XML_PATH);
    }

    /**
     * 该构造函数用于非集群环境
     *
     * @param applicationContext 本地的ApplicationContext上下文对象
     */
    public SpringJobBeanFactory(ApplicationContext applicationContext) {
        this.classLoader = applicationContext.getClassLoader();
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getJobBean(String className) {
        T instance;
        Class<T> clazz;
        try {
            clazz = (Class<T>) classLoader.loadClass(className);
        } catch (Throwable e) {
            throw new NiubiException(e);
        }
        try {
            instance = applicationContext.getBean(clazz);
        } catch (Throwable e) {
            LoggerHelper.warn("can't find instance for " + className);
            try {
                instance = clazz.newInstance();
            } catch (Throwable e1) {
                throw new NiubiException(e1);
            }
        }
        return instance;
    }

}
