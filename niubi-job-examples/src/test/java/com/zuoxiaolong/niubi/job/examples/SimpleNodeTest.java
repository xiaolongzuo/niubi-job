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

package com.zuoxiaolong.niubi.job.examples;

import com.zuoxiaolong.niubi.job.core.node.Node;
import com.zuoxiaolong.niubi.job.core.node.SimpleNode;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 16/1/9 02:19
 */
public class SimpleNodeTest {

    @org.junit.Test
    public void test() throws InterruptedException {
        Node node = new SimpleNode();
        node.join();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 启动容器");
        Thread.sleep(1000 * 40);
        node.exit();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 暂停容器");
        Thread.sleep(1000 * 30);
        node.join();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 恢复容器");
        Thread.sleep(1000 * 40);
        node.exit();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 关闭容器");
        Thread.sleep(1000 * 2);
    }

}
