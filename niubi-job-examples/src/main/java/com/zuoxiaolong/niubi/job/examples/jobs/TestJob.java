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

package com.zuoxiaolong.niubi.job.examples.jobs;

import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 16/1/11 23:37
 */
public class TestJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        LoggerHelper.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 这是一个TestJob");
    }

}
