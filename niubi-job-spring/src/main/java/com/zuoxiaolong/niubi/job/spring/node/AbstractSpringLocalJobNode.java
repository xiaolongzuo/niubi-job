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

import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;
import com.zuoxiaolong.niubi.job.scheduler.node.LocalJobNode;
import com.zuoxiaolong.niubi.job.spring.container.DefaultSpringContainer;
import org.springframework.context.ApplicationContext;

/**
 * @author Xiaolong Zuo
 * @since 16/1/16 16:49
 */
public class AbstractSpringLocalJobNode extends AbstractNode implements LocalJobNode {

    private Container container;

    public AbstractSpringLocalJobNode(ApplicationContext applicationContext, String packagesToScan, String[] propertiesFileNames) {
        super(applicationContext.getClassLoader(), propertiesFileNames);
        this.container = new DefaultSpringContainer(applicationContext, getConfiguration(), packagesToScan);
    }

    @Override
    public void join() {
        this.container.getScheduleManager().startup();
    }

    @Override
    public void exit() {
        this.container.getScheduleManager().shutdown();
    }

    @Override
    public Container getContainer() {
        return container;
    }

}
