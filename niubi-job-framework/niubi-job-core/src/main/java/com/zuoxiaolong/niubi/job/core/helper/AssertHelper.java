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
 * 断言帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface AssertHelper {

    /**
     * 检查对象是否为null.
     *
     * @param o 要检查的对象
     * @param message 如果检查失败,则使用该参数作为异常信息
     *
     * @throws IllegalArgumentException 如果检查的对象为{@code null}
     */
    static void notNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查对象是否为empty.
     *
     * @param o 要检查的对象
     * @param message 如果检查失败,则使用该参数作为异常信息
     *
     * @throws IllegalArgumentException 如果检查的对象为empty
     */
    static void notEmpty(Object o, String message) {
        if (o == null || o.toString().trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 检查结果是否为true
     *
     * @param b 要检查的结果
     * @param message 如果检查失败,则使用该参数作为异常信息
     *
     * @throws IllegalArgumentException 如果检查的结果为false
     */
    static void isTrue(boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

}
