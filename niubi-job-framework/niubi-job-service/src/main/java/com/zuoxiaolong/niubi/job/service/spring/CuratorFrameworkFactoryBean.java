/*
 * Copyright 2002-2015 the original author or authors.
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


package com.zuoxiaolong.niubi.job.service.spring;

import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import lombok.Setter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * ZK client的工厂Bean.用于在spring容器中构造一个ZK client.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class CuratorFrameworkFactoryBean implements FactoryBean<CuratorFramework>, InitializingBean {

    private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", 60 * 1000);

    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15 * 1000);

    private static final RetryPolicy DEFAULT_RETRY_POLICY = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);

    @Setter
    private String connectString;

    @Setter
    private Integer sessionTimeoutMs = DEFAULT_SESSION_TIMEOUT_MS;

    @Setter
    private Integer connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;

    @Setter
    private RetryPolicy retryPolicy = DEFAULT_RETRY_POLICY;

    private CuratorFramework client;

    @Override
    public CuratorFramework getObject() throws Exception {
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return CuratorFramework.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        AssertHelper.notNull(connectString, "connectString property must be set in config.properties.");
        client = CuratorFrameworkFactory.newClient(connectString, sessionTimeoutMs, connectionTimeoutMs, retryPolicy);
        client.start();
    }

}
