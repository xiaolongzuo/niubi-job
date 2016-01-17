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

package com.zuoxiaolong.niubi.job.scheduler.node;

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.container.DefaultContainer;

/**
 * 单机版实现
 *
 * @author Xiaolong Zuo
 * @since 16/1/12 01:17
 */
public class SimpleLocalJobNode extends AbstractLocalJobNode {

    private Container container;

    public SimpleLocalJobNode(String packagesToScan) {
        this(packagesToScan, StringHelper.emptyArray());
    }

    public SimpleLocalJobNode(String packagesToScan, String[] propertiesFileNames) {
        this(ClassHelper.getDefaultClassLoader(), packagesToScan, propertiesFileNames);
    }

    SimpleLocalJobNode(ClassLoader classLoader, String packagesToScan, String[] propertiesFileNames) {
        super(classLoader, packagesToScan, propertiesFileNames);
        this.container = new DefaultContainer(classLoader, packagesToScan);
    }

    @Override
    public Container getContainer() {
        return container;
    }

}
