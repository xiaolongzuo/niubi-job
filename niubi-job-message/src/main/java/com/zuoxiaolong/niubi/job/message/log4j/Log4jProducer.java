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


package com.zuoxiaolong.niubi.job.message.log4j;

import com.zuoxiaolong.niubi.job.message.Message;
import com.zuoxiaolong.niubi.job.message.Producer;

/**
 * @author Xiaolong Zuo
 * @since 1/14/2016 15:05
 */
public class Log4jProducer implements Producer<Log4jMessage.Data> {

    @Override
    public void sendMessage(Message<Log4jMessage.Data> message) {
        Log4jMessage.Data data = message.getData();
        if (data.getThrowable() != null) {
            LoggerHelper.error(data.getClazz(), data.getMessage(), data.getThrowable());
        } else {
            LoggerHelper.info(data.getClazz(), data.getMessage());
        }
    }

}
