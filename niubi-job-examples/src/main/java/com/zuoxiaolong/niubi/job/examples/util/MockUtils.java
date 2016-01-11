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

package com.zuoxiaolong.niubi.job.examples.util;

import com.zuoxiaolong.niubi.job.examples.jobs.EmptyJob;
import com.zuoxiaolong.niubi.job.examples.jobs.TestJob;
import org.quartz.*;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 00:11
 */
public abstract class MockUtils {

    public static JobDetail getTestJobDetail(String id) {
        return JobBuilder.newJob(TestJob.class).storeDurably().withIdentity("job-test-" + id).build();
    }

    public static Trigger getTestTrigger(String id) {
        return TriggerBuilder.newTrigger().forJob("job-test-" + id).withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")).build();
    }

    public static JobDetail getEmptyJobDetail(String id) {
        return JobBuilder.newJob(EmptyJob.class).storeDurably().withIdentity("job-empty-" + id).build();
    }

    public static Trigger getEmptyTrigger(String id) {
        return TriggerBuilder.newTrigger().forJob("job-empty-" + id).withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")).build();
    }

    public static TriggerKey getTestTriggerKey(String id) {
        return TriggerKey.triggerKey("job-test-" + id);
    }

    public static TriggerKey getEmptyTriggerKey(String id) {
        return TriggerKey.triggerKey("job-empty-" + id);
    }
}
