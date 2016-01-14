package com.zuoxiaolong.niubi.job.scheduler.job;

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

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * @author Xiaolong Zuo
 * @since 1/12/2016 17:44
 */
public class DefaultJobDescriptor extends AbstractKeyDescriptor implements JobDescriptor {

    private JobDataMap jobDataMap;

    DefaultJobDescriptor(String group, String name) {
        this(group, name, null);
    }

    DefaultJobDescriptor(String group, String name, JobDataMap jobDataMap) {
        super(group, name);
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
        this.jobDataMap = jobDataMap;
    }

    @Override
    public JobDataMap jobDataMap() {
        return jobDataMap;
    }

    @Override
    public JobDetail jobDetail() {
        return JobBuilder.newJob(StubJob.class)
                .withIdentity(name(), group())
                .storeDurably(true)
                .setJobData(jobDataMap)
                .build();
    }

}
