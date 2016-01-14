package com.zuoxiaolong.niubi.job.message.log4j;

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

import com.zuoxiaolong.niubi.job.message.AbstractMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 左潇龙
 * @since 1/14/2016 12:37
 */
public class Log4jMessage extends AbstractMessage<Log4jMessage.Data> {

    public Log4jMessage() {
    }

    public Log4jMessage(Data data) {
        super(data);
    }

    public static Log4jMessage.Data build(Class<?> clazz, String message) {
        return build(clazz, message, null);
    }

    public static Log4jMessage.Data build(Class<?> clazz, String message, Throwable throwable) {
        Data data = new Data();
        data.setClazz(clazz);
        data.setMessage(message);
        data.setThrowable(throwable);
        return data;
    }

    @Setter
    @Getter
    public static class Data {

        private Class<?> clazz;

        private String message;

        private Throwable throwable;

    }

}
