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

package com.zuoxiaolong.niubi.job.api.curator;

import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.StandbyJobApi;
import com.zuoxiaolong.niubi.job.api.StandbyNodeApi;
import com.zuoxiaolong.niubi.job.api.StandbyPathApi;
import org.apache.curator.framework.CuratorFramework;

/**
 * 主备模式API的工厂接口实现
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StandbyApiFactoryImpl implements StandbyApiFactory {

    private CuratorFramework client;

    public StandbyApiFactoryImpl(CuratorFramework client) {
        this.client = client;
    }

    @Override
    public StandbyPathApi pathApi() {
        return StandbyPathApiImpl.INSTANCE;
    }

    @Override
    public StandbyNodeApi nodeApi() {
        return new StandbyNodeApiImpl(client);
    }

    @Override
    public StandbyJobApi jobApi() {
        return new StandbyJobApiImpl(client);
    }

}
