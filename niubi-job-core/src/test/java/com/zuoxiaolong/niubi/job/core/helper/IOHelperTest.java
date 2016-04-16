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

import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class IOHelperTest {

    @Test
    public void writeFile() throws IOException {
        String filePath = System.getProperty("user.dir") + "/test.txt";
        IOHelper.writeFile(filePath, "hello".getBytes());
        File file = new File(filePath);
        Assert.assertTrue(file.exists());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        bufferedReader.close();
        Assert.assertEquals("hello", line);
        Assert.assertTrue(file.delete());
    }

    @Test
    public void readStreamBytes() throws IOException {
        String filePath = System.getProperty("user.dir") + "/test.txt";
        byte[] originBytes = "hello".getBytes();
        IOHelper.writeFile(filePath, originBytes);
        InputStream inputStream = new FileInputStream(filePath);
        byte[] bytes = IOHelper.readStreamBytes(inputStream);
        Assert.assertEquals(bytes.length, originBytes.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], originBytes[i]);
        }
        inputStream.close();
        File file = new File(filePath);
        Assert.assertTrue(file.delete());
    }

    @Test
    public void readStreamBytesAndClose() throws IOException {
        String filePath = System.getProperty("user.dir") + "/test.txt";
        byte[] originBytes = "hello".getBytes();
        IOHelper.writeFile(filePath, originBytes);
        InputStream inputStream = new FileInputStream(filePath);
        byte[] bytes = IOHelper.readStreamBytesAndClose(inputStream);
        Assert.assertEquals(bytes.length, originBytes.length);
        for (int i = 0; i < bytes.length; i++) {
            Assert.assertEquals(bytes[i], originBytes[i]);
        }
        try {
            inputStream.read();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Stream Closed"));
        }
        File file = new File(filePath);
        Assert.assertTrue(file.delete());
    }

}
