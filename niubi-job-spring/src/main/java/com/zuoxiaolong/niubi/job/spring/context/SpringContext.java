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

package com.zuoxiaolong.niubi.job.spring.context;

import com.zuoxiaolong.niubi.job.core.bean.JobBeanFactory;
import com.zuoxiaolong.niubi.job.core.config.Configuration;
import com.zuoxiaolong.niubi.job.core.config.Context;
import com.zuoxiaolong.niubi.job.core.config.DefaultContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Xiaolong Zuo
 * @since 16/1/11 22:44
 */
public class SpringContext extends DefaultContext implements Context, BeanFactoryAware, InitializingBean {

    private BeanFactory beanFactory;

    public SpringContext(ClassLoader classLoader, Configuration configuration) {
        super(classLoader, configuration);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public JobBeanFactory jobBeanFactory() {
        return super.jobBeanFactory();
    }

    @Override
    public Configuration configuration() {
        return super.configuration();
    }

}
