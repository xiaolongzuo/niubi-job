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

package com.zuoxiaolong.niubi.job.examples;

import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import com.zuoxiaolong.niubi.job.examples.jobs.TestJob;
import com.zuoxiaolong.niubi.job.examples.util.MockUtils;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/11 23:22
 */
public class QuartzTest {

    @Test
    public void testMisfire() throws SchedulerException, InterruptedException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        scheduler.scheduleJob(JobBuilder.newJob(TestJob.class).storeDurably().withIdentity("job-test").build(),
                TriggerBuilder.newTrigger().forJob("job-test")
                .withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")).build());
        Thread.sleep(10000);
        scheduler.pauseAll();
        Thread.sleep(70000);
        scheduler.resumeAll();
        Thread.sleep(10000);
    }

    @Test
    public void testQuartzAPI() throws SchedulerException, InterruptedException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        for (int i = 1 ; i < 10; i++) {
            scheduler.scheduleJob(MockUtils.getEmptyJobDetail(String.valueOf(i)), MockUtils.getEmptyTrigger(String.valueOf(i)));
        }
        Thread.sleep(5000);
        for (;;) {
            List<JobExecutionContext> jobExecutionContexts = scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext jobExecutionContext : jobExecutionContexts) {
                System.out.println(JsonHelper.toJson(jobExecutionContext.getJobDetail().getKey().getGroup()));
                System.out.println(JsonHelper.toJson(jobExecutionContext.getJobDetail().getKey().getName()));
                System.out.println(JsonHelper.toJson(scheduler.getTriggerState(jobExecutionContext.getTrigger().getKey())));
                System.out.println("----------------------");
            }
        }
    }

}
