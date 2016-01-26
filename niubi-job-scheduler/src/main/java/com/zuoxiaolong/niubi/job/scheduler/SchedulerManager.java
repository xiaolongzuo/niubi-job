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

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface SchedulerManager {

    void startup();

    void startup(String group);

    void startup(String group, String name);

    void startupManual(String cron, String misfirePolicy);

    void startupManual(String group, String cron, String misfirePolicy);

    void startupManual(String group, String name, String cron, String misfirePolicy);

    void shutdown();

    void shutdown(String group);

    void shutdown(String group, String name);

    List<String> getGroupList();

    List<String> getNameList(String group);

    ScheduleStatus getScheduleStatus(String group, String name);

}
