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

import com.zuoxiaolong.niubi.job.api.PathApi;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 01:03
 */
public class PathApiImpl implements PathApi {

    public static final PathApi INSTANCE = new PathApiImpl();

    private static final String ROOT_PATH = "/job-root";

    private static final String STANDBY_NODE_PATH = ROOT_PATH + "/standby-node";

    private static final String MASTER_SLAVE_NODE_PATH = ROOT_PATH + "/master-slave-node";

    private PathApiImpl() {}

    @Override
    public String getStandbyNodePath() {
        return STANDBY_NODE_PATH + "/nodes/child";
    }

    @Override
    public String getStandbyMasterPath() {
        return STANDBY_NODE_PATH + "/master";
    }

    @Override
    public String getStandbyJobPath() {
        return STANDBY_NODE_PATH + "/jobs";
    }

    @Override
    public String getMasterSlaveMasterSelectorPath() {
        return MASTER_SLAVE_NODE_PATH + "/master-selector";
    }

    @Override
    public String getMasterSlaveEphemeralNodePath() {
        return MASTER_SLAVE_NODE_PATH + "/ephemeral-node";
    }

    @Override
    public String getMasterSlavePersistentNodePath() {
        return MASTER_SLAVE_NODE_PATH + "/persistent-node";
    }

    @Override
    public String getMasterSlaveCounterPath() {
        return MASTER_SLAVE_NODE_PATH + "/counter";
    }

    @Override
    public String getMasterSlaveLockPath() {
        return MASTER_SLAVE_NODE_PATH + "/lock";
    }

}
