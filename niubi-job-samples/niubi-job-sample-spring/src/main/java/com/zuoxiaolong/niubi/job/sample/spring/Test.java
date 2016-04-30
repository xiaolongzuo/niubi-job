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


package com.zuoxiaolong.niubi.job.sample.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * use to test jobs.
 *
 * @author Xiaolong Zuo
 * @since 1/22/2016 14:19
 */
public class Test {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("applicationContext.xml");
    }

}
