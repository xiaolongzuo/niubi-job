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

package com.zuoxiaolong.niubi.job.spring.config;

import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoader;
import com.zuoxiaolong.niubi.job.spring.node.SimpleSpringLocalJobNode;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * spring环境下任务的驱动器,用于启动任务容器
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class SpringContextJobDriver implements ApplicationContextAware {

    @Setter
    private ApplicationContext applicationContext;

    @Setter
    private String packagesToScan;

    public void init() {
        ClassLoader classLoader = applicationContext.getClassLoader();
        boolean isSimpleMode = !(classLoader instanceof ApplicationClassLoader);
        //avoid dead cycle
        if (isSimpleMode) {
            new SimpleSpringLocalJobNode(applicationContext, packagesToScan).join();
        }
    }

}
