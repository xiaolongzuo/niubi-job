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

import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scheduler.config.Configuration;
import org.apache.log4j.PropertyConfigurator;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 23:42
 */
public abstract class AbstractNode implements Node {

    private String ip;

    private Configuration configuration;

    public AbstractNode(ClassLoader classLoader, String[] propertiesFileNames) {
        this.configuration = new Configuration(classLoader);
        if (!StringHelper.isEmpty(propertiesFileNames)) {
            for (String propertiesFileName : propertiesFileNames) {
                this.configuration.addProperties(propertiesFileName);
            }
        }
        PropertyConfigurator.configure(this.configuration.getProperties());
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.ip = "unknown";
        }
    }

    protected String getIp() {
        return ip;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

}
