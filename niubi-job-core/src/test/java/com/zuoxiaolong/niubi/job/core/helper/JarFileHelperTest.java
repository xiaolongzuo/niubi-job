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
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class JarFileHelperTest {

    @Test
    public void getJarFileName() {
        Assert.assertNull(JarFileHelper.getJarFileName(null));
        Assert.assertTrue("1.jar".equals(JarFileHelper.getJarFileName("1.jar")));
        Assert.assertTrue("1.jar".equals(JarFileHelper.getJarFileName("C:/1.jar")));
    }

    @Test
    public void downloadJarFile() throws IOException {
        HttpServer.start();
        String filePath = System.getProperty("user.dir");
        String jarFilePath = JarFileHelper.downloadJarFile(filePath, "http://localhost:8080/download/test.jar");
        File file = new File(jarFilePath);
        Assert.assertTrue(file.exists());
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = IOHelper.readStreamBytesAndClose(fileInputStream);
        Assert.assertEquals(new String(bytes), "hello");
        Assert.assertTrue(file.delete());
        HttpServer.exit();
    }

}
