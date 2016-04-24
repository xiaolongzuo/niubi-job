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

import com.zuoxiaolong.niubi.job.test.helper.FileHelper;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;

import java.io.File;
import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class ZookeeperServerCluster {

    private ZookeeperServerCluster() {}

    private static TestingCluster server;

    private static final String DATA_DIR = System.getProperty("user.dir") + "/target/zk";

    public static synchronized void startZookeeperCluster() {
        File file1 = new File(DATA_DIR + "1");
        File file2 = new File(DATA_DIR + "2");
        File file3 = new File(DATA_DIR + "3");
        file1.mkdirs();
        file2.mkdirs();
        file3.mkdirs();
        InstanceSpec instanceSpec1 = new InstanceSpec(file1, 2182, 2988, 3988,true, 1);
        InstanceSpec instanceSpec2 = new InstanceSpec(file2, 3182, 2989, 3989,true, 2);
        InstanceSpec instanceSpec3 = new InstanceSpec(file3, 4182, 2990, 3990,true, 3);
        if (server == null) {
            server = new TestingCluster(instanceSpec1, instanceSpec2, instanceSpec3);
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized void stopZookeeperCluster() {
        if (server != null) {
            try {
                server.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server = null;
            File file1 = new File(DATA_DIR + "1");
            File file2 = new File(DATA_DIR + "2");
            File file3 = new File(DATA_DIR + "3");
            FileHelper.deleteDir(file1);
            FileHelper.deleteDir(file2);
            FileHelper.deleteDir(file3);
        }
    }

}
