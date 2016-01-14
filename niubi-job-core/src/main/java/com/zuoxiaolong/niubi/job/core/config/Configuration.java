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

package com.zuoxiaolong.niubi.job.core.config;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.io.ClasspathResource;
import com.zuoxiaolong.niubi.job.core.io.FileSystemResource;
import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

/**
 * job框架的基本配置
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 04:15
 */
public class Configuration {

    private static final String DEFAULT_QUARTZ_FILE = "quartz-default.properties";

    private static final String DEFAULT_LOG4J_FILE = "log4j-default.properties";

    @Getter
    private Properties properties;

    private ClassLoader classLoader;

    public Configuration(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.properties = new Properties();
        load(DEFAULT_QUARTZ_FILE, true);
        load(DEFAULT_LOG4J_FILE, true);
    }

    public void addProperties(String propertiesFileName) {
        try {
            this.properties.load(new FileSystemResource(propertiesFileName).getInputStream());
        } catch (IOException e) {
            LoggerHelper.error("read config file [" + propertiesFileName + "] failed.", e);
            throw new NiubiException(e);
        }
    }

    private void load(String propertiesFileName, boolean throwException) {
        try {
            this.properties.load(new ClasspathResource(classLoader, propertiesFileName).getInputStream());
        } catch (IOException e) {
            LoggerHelper.error("read config file [" + propertiesFileName + "] failed.", e);
            if (throwException) {
                throw new NiubiException(e);
            }
        }
    }

}
