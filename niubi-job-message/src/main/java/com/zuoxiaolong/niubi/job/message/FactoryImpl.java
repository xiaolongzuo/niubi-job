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


package com.zuoxiaolong.niubi.job.message;

import com.zuoxiaolong.niubi.job.message.log4j.Log4jMessage;
import com.zuoxiaolong.niubi.job.message.log4j.Log4jProducer;

/**
 * @author Xiaolong Zuo
 * @since 1/14/2016 15:36
 */
public class FactoryImpl implements Factory {

    @Override
    public <T> Producer<T> createProducer() {
        return (Producer<T>) new Log4jProducer();
    }

    @Override
    public <T, E> Message<T> createMessage(E data) {
        return (Message<T>) new Log4jMessage((Log4jMessage.Data) data);
    }

}
