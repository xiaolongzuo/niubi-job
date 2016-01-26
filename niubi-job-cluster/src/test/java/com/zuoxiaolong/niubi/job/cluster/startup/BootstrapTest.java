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


package com.zuoxiaolong.niubi.job.cluster.startup;

import org.apache.curator.test.TestingServer;
import org.junit.Test;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class BootstrapTest {

    @Test
    public void start() throws Exception {
        TestingServer testingServer1 = new TestingServer(2181);
        TestingServer testingServer2 = new TestingServer(3181);
        TestingServer testingServer3 = new TestingServer(4181);
        testingServer1.start();
        testingServer2.start();
        testingServer3.start();

        Bootstrap.start();
        Thread.sleep(2000);
        Bootstrap.stop();
    }

}
