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

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.io.ClasspathResource;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Properties;

/**
 * job框架的基本配置
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 04:15
 */
@Setter
@Getter
public class Configuration {

    private static final String DEFAULT_CONFIG_FILE = "job-config.properties";
    private static final String DEFAULT_QUARTZ_FILE = "quartz-default.properties";

    private Properties properties;

    public Configuration(ClassLoader classLoader) {
        this(classLoader, new Properties());
    }

    public Configuration(ClassLoader classLoader, Properties properties) {
        this.properties = properties;
        try {
            this.properties.load(new ClasspathResource(classLoader, DEFAULT_QUARTZ_FILE).getInputStream());
        } catch (IOException e) {
            LoggerHelper.error("read config file [" + DEFAULT_QUARTZ_FILE + "] failed.", e);
            throw new NiubiException(e);
        }
        try {
            this.properties.load(new ClasspathResource(classLoader, DEFAULT_CONFIG_FILE).getInputStream());
        } catch (Exception e) {
            LoggerHelper.info("read config file [" + DEFAULT_CONFIG_FILE + "] failed, has been ignored.", e);
        }
    }

}
