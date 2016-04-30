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

package com.zuoxiaolong.niubi.job.scanner;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.text.resources.cldr.aa.FormatData_aa;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4
 */
public class ApplicationClassLoaderTest {

    @BeforeClass
    public static void setUp() {
        ApplicationClassLoaderFactory.setSystemClassLoader(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void testNodeClassLoader() throws ClassNotFoundException {
        ApplicationClassLoader applicationClassLoader = ApplicationClassLoaderFactory.getNodeApplicationClassLoader();
        Assert.assertTrue(applicationClassLoader.loadClass("java.lang.String") == String.class);
        Assert.assertTrue(applicationClassLoader.loadClass("sun.text.resources.cldr.aa.FormatData_aa") == FormatData_aa.class);
        Assert.assertTrue(applicationClassLoader.loadClass("com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderTest") == ApplicationClassLoaderTest.class);
        Assert.assertTrue(applicationClassLoader.loadClass("com.zuoxiaolong.niubi.job.core.exception.NiubiException") == NiubiException.class);
    }

}
