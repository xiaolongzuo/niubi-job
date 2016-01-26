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

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class ExceptionHelper {

    private static final int MAX_STACK_TRACE_DEEP = 20;

    public static String getStackTrace() {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Throwable throwable = new Throwable();
            StackTraceElement[] stackElements = throwable.getStackTrace();
            if (stackElements != null) {
                for (int i = 0; i < stackElements.length && i < MAX_STACK_TRACE_DEEP; i++) {
                    if (i > 0) {
                        stringBuffer.append("\t");
                    }
                    stringBuffer.append(stackElements[i].getClassName()).append(".");
                    stringBuffer.append(stackElements[i].getMethodName()).append("(");
                    stringBuffer.append(stackElements[i].getFileName()).append(":");
                    stringBuffer.append(stackElements[i].getLineNumber()).append(")");
                    stringBuffer.append("\r\n");
                }
            }
        } catch (Exception e) {
            //ignored
        }
        return stringBuffer.toString();
    }

}
