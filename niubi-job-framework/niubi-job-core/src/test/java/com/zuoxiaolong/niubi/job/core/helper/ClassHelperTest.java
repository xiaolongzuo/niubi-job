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

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class ClassHelperTest {

    @Test
    public void getPackageName() {
        Assert.assertEquals("com.zuoxiaolong", ClassHelper.getPackageName("com.zuoxiaolong.Test"));
    }

    @Test
    public void getClassName() {
        Assert.assertEquals("com.zuoxiaolong.Test", ClassHelper.getClassName("com/zuoxiaolong/Test.class"));
    }

    @Test
    public void getDefaultClassLoader() {
        Assert.assertEquals(Thread.currentThread().getContextClassLoader(), ClassHelper.getDefaultClassLoader());
    }

    @Test
    public void overrideThreadContextClassLoader() {
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };
        ClassHelper.overrideThreadContextClassLoader(classLoader);
        Assert.assertEquals(classLoader, ClassHelper.getDefaultClassLoader());
    }

}
