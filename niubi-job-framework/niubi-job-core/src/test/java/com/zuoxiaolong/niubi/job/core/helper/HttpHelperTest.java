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

package com.zuoxiaolong.niubi.job.core.helper;

import com.zuoxiaolong.niubi.job.test.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class HttpHelperTest {

    @BeforeClass
    public static void setup() {
        HttpServer.start();
    }

    @AfterClass
    public static void teardown() {
        HttpServer.exit();
    }

    @Test
    public void downloadRemoteResource() throws IOException {
        String filePath = System.getProperty("user.dir") + "/test.txt";
        String returnedFilePath = HttpHelper.downloadRemoteResource(filePath, "http://localhost:8080/download/test.txt");
        Assert.assertTrue(filePath.equals(returnedFilePath));
        File file = new File(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String text = reader.readLine().trim();
        reader.close();
        Assert.assertTrue(text.equals("hello"));
        Assert.assertTrue(file.delete());
    }

}
