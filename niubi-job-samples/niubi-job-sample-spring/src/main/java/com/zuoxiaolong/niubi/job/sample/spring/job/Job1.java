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

package com.zuoxiaolong.niubi.job.sample.spring.job;

import com.zuoxiaolong.niubi.job.sample.spring.bean.OneService;
import com.zuoxiaolong.niubi.job.scanner.annotation.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Xiaolong Zuo
 * @since 16/1/16 15:30
 */
@Component
public class Job1 {

    @Autowired
    private OneService oneService;

    @Schedule(cron = "0/15 * * * * ?")
    public void test() {
        oneService.someServiceMethod1();
    }

}
