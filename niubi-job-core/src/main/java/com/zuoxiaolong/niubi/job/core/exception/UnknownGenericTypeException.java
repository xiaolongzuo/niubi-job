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

package com.zuoxiaolong.niubi.job.core.exception;

/**
 * Represent a unknown generic type exception
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class UnknownGenericTypeException extends RuntimeException {

    public UnknownGenericTypeException() {
    }

    public UnknownGenericTypeException(String message) {
        super(message);
    }

    public UnknownGenericTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownGenericTypeException(Throwable cause) {
        super(cause);
    }

    public UnknownGenericTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
