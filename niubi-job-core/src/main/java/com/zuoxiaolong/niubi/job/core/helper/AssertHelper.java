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

/**
 * Assert util.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AssertHelper {

    /**
     * Assert that the object is not {@code null}
     *
     * @param o the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void notNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Object o, String message) {
        if (o == null || o.toString().trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

}
