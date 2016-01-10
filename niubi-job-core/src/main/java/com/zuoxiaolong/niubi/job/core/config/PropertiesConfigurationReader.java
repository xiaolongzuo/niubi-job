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

import com.zuoxiaolong.niubi.job.core.Resource;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 23:55
 */
public class PropertiesConfigurationReader implements ConfigurationReader {

    public Configuration read(Resource... resources) {
        Properties properties = new Properties();
        for (Resource resource : resources) {
            try {
                properties.load(resource.getInputStream());
            } catch (IOException e) {
                LoggerHelper.warn("load properties failed [" + resource + "]", e);
            }
        }
        return null;
    }

}
