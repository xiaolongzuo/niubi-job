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

package com.zuoxiaolong.niubi.job.test.zookeeper;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;

import java.io.File;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public interface ZookeeperServerCluster {

    static void startZookeeperCluster() {
        InstanceSpec instanceSpec1 = new InstanceSpec(new File(System.getProperty("user.dir") + "/target"), 2181, 2888, 3888,true, 1);
        InstanceSpec instanceSpec2 = new InstanceSpec(new File(System.getProperty("user.dir") + "/target"), 3181, 2889, 3889,true, 2);
        InstanceSpec instanceSpec3 = new InstanceSpec(new File(System.getProperty("user.dir") + "/target"), 4181, 2890, 3890,true, 3);
        TestingCluster server = new TestingCluster(instanceSpec1, instanceSpec2, instanceSpec3);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
