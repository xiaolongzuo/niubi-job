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

package com.zuoxiaolong.niubi.job.scheduler;

/**
 * 手动控制的调度管理器.
 * 在调度一个任务时,需要制定任务的运行环境和cron表达式
 *
 * @author Xiaolong Zuo
 * @since 0.9.4
 */
public interface ManualScheduleManager extends ScheduleManager {

    void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String cron, String misfirePolicy);

    void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String group, String cron, String misfirePolicy);

    void startupManual(String jarFilePath, String packagesToScan, boolean isSpring, String group, String name, String cron, String misfirePolicy);

}
