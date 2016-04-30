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

import com.google.gson.Gson;

/**
 * 基于GSON的JSON解析器
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class JsonHelper {

    private static final Gson GSON = new Gson();

    public static byte[] toBytes(Object object) {
        return StringHelper.getBytes(toJson(object));
    }

    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(byte[] bytes, Class<T> clazz) {
        return fromJson(StringHelper.getString(bytes), clazz);
    }

}
