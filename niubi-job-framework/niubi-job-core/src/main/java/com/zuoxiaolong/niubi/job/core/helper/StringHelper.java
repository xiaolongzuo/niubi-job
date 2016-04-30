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
import java.util.Arrays;
import java.util.List;

/**
 * String帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class StringHelper {

    private static final String charset = "UTF-8";

    public static String appendSlant(String url) {
        if (url == null ) {
            return null;
        }
        url = url.replace("\\", "/");
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

    public static String getString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            //ignored
            return "";
        }
    }

    public static String[] mergeArray(String[] a, String[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        String[] newArray = new String[a.length + b.length];
        System.arraycopy(a, 0, newArray, 0, a.length);
        System.arraycopy(b, 0, newArray, a.length, b.length);
        return newArray;
    }

    public static String[] emptyArray() {
        return new String[0];
    }

    public static String emptyString() {
        return "";
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmpty(String[] s) {
        return s == null || s.length == 0;
    }

    public static String[] checkEmpty(String[] s) {
        return ListHelper.isEmpty(s) ? emptyArray() : s;
    }

    public static String isEmpty(String value, String defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static String[] split(String s) {
        if (isEmpty(s)) {
            return new String[0];
        }
        return s.split(",|;|:");
    }

    public static List<String> splitToList(String s) {
        return Arrays.asList(split(s));
    }

}
