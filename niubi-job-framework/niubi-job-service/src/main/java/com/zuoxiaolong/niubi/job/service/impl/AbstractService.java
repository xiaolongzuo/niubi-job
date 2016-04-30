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


package com.zuoxiaolong.niubi.job.service.impl;

import com.zuoxiaolong.niubi.job.api.MasterSlaveApiFactory;
import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.curator.MasterSlaveApiFactoryImpl;
import com.zuoxiaolong.niubi.job.api.curator.StandbyApiFactoryImpl;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 抽象的服务实现类.
 * 它包含了主从和主备API工厂实例,可以帮助子类方便的进行ZK操作.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class AbstractService implements InitializingBean {

    @Autowired
    private CuratorFramework client;

    protected StandbyApiFactory standbyApiFactory;

    protected MasterSlaveApiFactory masterSlaveApiFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.standbyApiFactory = new StandbyApiFactoryImpl(client);
        this.masterSlaveApiFactory = new MasterSlaveApiFactoryImpl(client);
    }

}
