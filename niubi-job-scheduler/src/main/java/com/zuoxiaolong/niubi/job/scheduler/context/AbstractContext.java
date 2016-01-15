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

package com.zuoxiaolong.niubi.job.scheduler.context;

import com.zuoxiaolong.niubi.job.core.config.Configuration;
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.JobScanClassLoader;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:23
 */
public abstract class AbstractContext implements Context {

    private JobScanClassLoader classLoader;

    private Configuration configuration;

    public AbstractContext(ClassLoader classLoader, String[] propertiesFileNames) {
        AssertHelper.notNull(classLoader, "classLoader can't be null.");
        AssertHelper.notNull(classLoader, "jobBeanFactory can't be null.");
        this.classLoader = new JobScanClassLoader(classLoader);
        this.configuration = new Configuration(this.classLoader);
        if (!StringHelper.isEmpty(propertiesFileNames)) {
            for (String propertiesFileName : propertiesFileNames) {
                this.configuration.addProperties(propertiesFileName);
            }
        }
    }

    @Override
    public JobScanClassLoader classLoader() {
        return classLoader;
    }

    public Configuration configuration() {
        return configuration;
    }

}
