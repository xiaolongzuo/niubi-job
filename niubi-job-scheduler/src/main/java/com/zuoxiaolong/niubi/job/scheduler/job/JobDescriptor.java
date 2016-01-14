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

import com.zuoxiaolong.niubi.job.scheduler.annotation.MisfirePolicy;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import java.lang.reflect.Method;

/**
 * @author Xiaolong Zuo
 * @since 1/12/2016 17:38
 */
public interface JobDescriptor extends KeyDescriptor {

    String DATA_MAP_KEY = "_job_detail";

    Method method();

    boolean hasParameter();

    Class<?> clazz();

    String cron();

    MisfirePolicy misfirePolicy();

    JobDetail jobDetail();

    JobDataMap jobDataMap();

    Trigger trigger();

    boolean isManualTrigger();

    JobDescriptor withTrigger(String cron, MisfirePolicy misfirePolicy);

}
