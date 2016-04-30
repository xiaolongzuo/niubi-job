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

import com.zuoxiaolong.niubi.job.api.StandbyPathApi;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public final class StandbyPathApiImpl implements StandbyPathApi {

    public static final StandbyPathApi INSTANCE = new StandbyPathApiImpl();

    private static final String ROOT_PATH = "/job-root";

    private static final String STANDBY_NODE_PATH = ROOT_PATH + "/standby-node";

    private StandbyPathApiImpl() {}

    @Override
    public String getInitLockPath() {
        return STANDBY_NODE_PATH + "/initLock";
    }

    @Override
    public String getNodePath() {
        return STANDBY_NODE_PATH + "/nodes/child";
    }

    @Override
    public String getSelectorPath() {
        return STANDBY_NODE_PATH + "/selector";
    }

    @Override
    public String getJobPath() {
        return STANDBY_NODE_PATH + "/jobs";
    }

}
