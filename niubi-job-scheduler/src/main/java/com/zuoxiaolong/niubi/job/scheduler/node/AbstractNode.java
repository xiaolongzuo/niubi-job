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

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractNode implements Node {

    private String ip;

    /**
     * for local
     */
    public AbstractNode() {
        Properties properties = new Properties();
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("com/zuoxiaolong/niubi/job/scheduler/node/log4j-default.properties"));
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        try {
            properties.load(ClassHelper.getDefaultClassLoader().getResourceAsStream("log4j.properties"));
        } catch (Exception e) {
            LoggerHelper.warn("log4j properties not found ,use default instead.");
        }
        PropertyConfigurator.configure(properties);
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.ip = "unknown";
        }
    }
    /**
     * for remote
     * @param properties
     */
    public AbstractNode(Properties properties) {
        PropertyConfigurator.configure(properties);
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.ip = "unknown";
        }
    }

    protected String getIp() {
        return ip;
    }

}
