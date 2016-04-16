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

import org.apache.log4j.Logger;

/**
 * 日志工具类.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class LoggerHelper {

    public static void info(Class<?> clazz, String message) {
        info(clazz, message, null);
    }

    public static void info(String message) {
        info(null, message, null);
    }

    public static void info(String message, Throwable throwable) {
        info(null, message, throwable);
    }

    public static void info(Class<?> clazz, String message, Throwable throwable) {
        AssertHelper.notNull(message, "message can't be null!");
        if (clazz == null) {
            clazz = LoggerHelper.class;
        }
        Logger logger = Logger.getLogger(clazz);
        if (logger.isInfoEnabled() && throwable == null) {
            logger.info(message);
        }
        if (logger.isInfoEnabled() && throwable != null) {
            logger.info(message, throwable);
        }
    }

    public static void debug(String message) {
        debug(null, message, null);
    }

    public static void debug(String message, Throwable throwable) {
        debug(null, message, throwable);
    }

    public static void debug(Class<?> clazz, String message, Throwable throwable) {
        AssertHelper.notNull(message, "message can't be null!");
        if (clazz == null) {
            clazz = LoggerHelper.class;
        }
        Logger logger = Logger.getLogger(clazz);
        if (logger.isDebugEnabled() && throwable == null) {
            logger.debug(message);
        }
        if (logger.isDebugEnabled() && throwable != null) {
            logger.debug(message, throwable);
        }
    }

    public static void warn(String message) {
        warn(null, message, null);
    }

    public static void warn(String message, Throwable throwable) {
        warn(null, message, throwable);
    }

    public static void warn(Class<?> clazz, String message, Throwable throwable) {
        AssertHelper.notNull(message, "message can't be null!");
        if (clazz == null) {
            clazz = LoggerHelper.class;
        }
        Logger logger = Logger.getLogger(clazz);
        if (throwable == null) {
            logger.warn(message);
        }
        if (throwable != null) {
            logger.warn(message, throwable);
        }
    }

    public static void error(String message) {
        error(null, message, null);
    }

    public static void error(Class<?> clazz, String message) {
        error(clazz, message, null);
    }

    public static void error(String message, Throwable throwable) {
        error(null, message, throwable);
    }

    public static void error(Class<?> clazz, String message, Throwable throwable) {
        if (clazz == null) {
            clazz = LoggerHelper.class;
        }
        AssertHelper.notNull(message, "message can't be null!");
        AssertHelper.notNull(throwable, "throwable can't be null!");
        Logger logger = Logger.getLogger(clazz);
        logger.error(message, throwable);
    }

}
