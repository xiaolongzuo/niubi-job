package com.zuoxiaolong.niubi.job.api.config;

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

import com.zuoxiaolong.niubi.job.core.config.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 左潇龙
 * @since 1/12/2016 12:29
 */
@Setter
@Getter
public class ClusterConfiguration extends Configuration {

    private static final String PROPERTY_NAME_CONNECT_STRING = "zk.connect.string";
    private static final String PROPERTY_NAME_ROOT_PATH = "zk.root.path";

    private static final String MASTER_SLAVE_NODE_PATH = "/master-slave-node-path";
    private static final String MASTER_SELECTOR_PATH = MASTER_SLAVE_NODE_PATH + "/master-selector";
    private static final String NODE_EPHEMERAL_PATH = MASTER_SLAVE_NODE_PATH + "/ephemeral-path";
    private static final String NODE_PERSISTENT_PATH = MASTER_SLAVE_NODE_PATH + "/persistent-path";
    private static final String COUNTER_PATH = MASTER_SLAVE_NODE_PATH + "/counter-path";
    private static final String LOCK_PATH = MASTER_SLAVE_NODE_PATH + "/lock-path";

    private String connectString;

    private String rootPath;

    private String masterSelectorPath;

    private String ephemeralPath;

    private String persistentPath;

    private String counterPath;

    private String lockPath;

    @Override
    protected void readConfigurationFromProperties() {
        super.readConfigurationFromProperties();
        this.connectString = getProperties().getProperty(PROPERTY_NAME_CONNECT_STRING, "localhost:2181");
        this.rootPath = getProperties().getProperty(PROPERTY_NAME_ROOT_PATH, "/job-root");
        this.masterSelectorPath = this.rootPath + MASTER_SELECTOR_PATH;
        this.ephemeralPath = this.rootPath + NODE_EPHEMERAL_PATH;
        this.persistentPath = this.rootPath + NODE_PERSISTENT_PATH;
        this.counterPath = this.rootPath + COUNTER_PATH;
        this.lockPath = this.rootPath + LOCK_PATH;
    }

}
