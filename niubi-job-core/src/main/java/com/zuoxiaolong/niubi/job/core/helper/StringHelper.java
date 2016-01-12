package com.zuoxiaolong.niubi.job.core.helper;

/*
 * Copyright 2002-2015 the original author or authors.
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

import java.io.UnsupportedEncodingException;

/**
 * @author 左潇龙
 * @since 1/12/2016 12:19
 */
public abstract class StringHelper {

    private static final String charset = "UTF-8";

    public static String appendSlant(String url) {
        if (url == null ) {
            return null;
        }
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }

    public static byte[] getBytes(String s) {
        if (s == null) {
            return new byte[0];
        }
        try {
            return s.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            //ignored
            return new byte[0];
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String isEmpty(String value, String defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static String[] split(String s) {
        return s.split(",|;|:");
    }

}
