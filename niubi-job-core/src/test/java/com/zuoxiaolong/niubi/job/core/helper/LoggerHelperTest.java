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

import org.junit.Test;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class LoggerHelperTest {

    @Test
    public void debug() {
        LoggerHelper.debug(LoggerHelperTest.class, "test", null);
        LoggerHelper.debug(null, "test", null);
        LoggerHelper.debug(LoggerHelperTest.class, "test", new Throwable());
    }

    @Test
    public void info() {
        LoggerHelper.info(LoggerHelperTest.class, "test", null);
        LoggerHelper.info(null, "test", null);
        LoggerHelper.info(LoggerHelperTest.class, "test", new Throwable());
    }

    @Test
    public void warn() {
        LoggerHelper.warn(LoggerHelperTest.class, "test", null);
        LoggerHelper.warn(null, "test", null);
        LoggerHelper.warn(LoggerHelperTest.class, "test", new Throwable());
    }

    @Test
    public void error() {
        LoggerHelper.error(null, "test", new Throwable());
        LoggerHelper.error(LoggerHelperTest.class, "test", new Throwable());
    }

}
