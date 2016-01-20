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

package com.zuoxiaolong.niubi.job.spring.node;

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractLocalJobNode;
import com.zuoxiaolong.niubi.job.spring.container.DefaultSpringContainer;
import org.springframework.context.ApplicationContext;

/**
 * For local spring application.
 *
 * @author Xiaolong Zuo
 * @since 16/1/16 17:01
 */
public class SimpleSpringLocalJobNode extends AbstractLocalJobNode {

    private Container container;

    public SimpleSpringLocalJobNode(ApplicationContext applicationContext, String packagesToScan) {
        ClassHelper.overrideThreadContextClassLoader(applicationContext.getClassLoader());
        this.container = new DefaultSpringContainer(applicationContext, packagesToScan);
    }

    @Override
    public Container getContainer() {
        return container;
    }
}
