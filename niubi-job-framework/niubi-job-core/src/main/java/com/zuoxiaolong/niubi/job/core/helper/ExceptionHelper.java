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

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;

/**
 * 异常帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class ExceptionHelper {

    private static final int MAX_STACK_TRACE_DEEP = 20;

    private static final String DEFAULT_END_STRING = "...";

    private static final int MAX_LENGTH = 5000 - DEFAULT_END_STRING.length();

    /**
     * 获取throwable对象的堆栈信息
     *
     * @param throwable 异常
     * @param isHtmlStyle 是否需要html格式
     * @return 堆栈信息
     */
    public static String getStackTrace(Throwable throwable, boolean isHtmlStyle) {
        AssertHelper.notNull(throwable, "throwable can't be null.");
        String line = isHtmlStyle ? "<br/>" : "\r\n";
        String tab = isHtmlStyle ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "\t";
        while (throwable instanceof NiubiException) {
            throwable = throwable.getCause();
        }
        StringBuffer stringBuffer = new StringBuffer(throwable.getClass().getName()).append(":");
        if (throwable.getMessage() != null) {
            stringBuffer.append(throwable.getMessage()).append(line);
        } else {
            stringBuffer.append(line);
        }
        try {
            StackTraceElement[] stackElements = throwable.getStackTrace();
            if (stackElements != null) {
                for (int i = 0; i < stackElements.length && i < MAX_STACK_TRACE_DEEP; i++) {
                    stringBuffer.append(tab);
                    stringBuffer.append(stackElements[i].getClassName()).append('.');
                    stringBuffer.append(stackElements[i].getMethodName()).append('(');
                    stringBuffer.append(stackElements[i].getFileName()).append(':');
                    stringBuffer.append(stackElements[i].getLineNumber()).append(')');
                    stringBuffer.append(line);
                }
            }
        } catch (Exception e) {
            //ignored
        }
        if (isHtmlStyle) {
            return stringBuffer.length() > MAX_LENGTH ? (stringBuffer.substring(0, stringBuffer.lastIndexOf(line)) + DEFAULT_END_STRING) : stringBuffer.toString();
        } else {
            return stringBuffer.length() > MAX_LENGTH ? (stringBuffer.substring(0, MAX_LENGTH) + DEFAULT_END_STRING) : stringBuffer.toString();
        }
    }

    /**
     * @see ExceptionHelper#getStackTrace(Throwable, boolean)
     */
    public static String getStackTrace(Throwable throwable) {
        return getStackTrace(throwable, false);
    }

    /**
     * @see ExceptionHelper#getStackTrace(Throwable)
     */
    public static String getStackTrace() {
        return getStackTrace(new Throwable());
    }

}
