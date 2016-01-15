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
 * 日志工具类
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 00:54
 */
public abstract class LoggerHelper {

    private static final Logger logger = Logger.getLogger(LoggerHelper.class);

    public static void info(Class<?> clazz, String message) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public static void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public static void info(String message, Throwable throwable) {
        if (logger.isInfoEnabled()) {
            logger.info(message, throwable);
        }
    }

    public static void info(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isInfoEnabled()) {
            logger.info(message, throwable);
        }
    }

    public static void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public static void debug(String message, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, throwable);
        }
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(Class<?> clazz, String message) {
        Logger logger = Logger.getLogger(clazz);
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void error(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = Logger.getLogger(clazz);
        logger.error(message, throwable);
    }

}
