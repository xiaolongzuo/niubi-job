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
 * @author Xiaolong Zuo
 * @since 1/13/2016 14:25
 */
public class SpringJobBeanFactory implements JobBeanFactory {

    private ApplicationContext applicationContext;

    /**
     * for remote
     * @throws BeansException
     */
    public SpringJobBeanFactory(ClassLoader classLoader) throws BeansException {
        ClassUtils.overrideThreadContextClassLoader(classLoader);
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
    public <T> T getJobBean(Class<T> clazz) {
        T instance;
        try {
            instance = applicationContext.getBean(clazz);
        } catch (Throwable e) {
            LoggerHelper.warn("can't find instance for " + clazz);
            try {
                instance = clazz.newInstance();
            } catch (Throwable e1) {
                throw new NiubiException(e1);
            }
        }
        return instance;
    }

}
