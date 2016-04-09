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
 * 调度状态.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public enum ScheduleStatus {

    /**
     * 代表该任务已经启动
     */
    STARTUP,
    /**
     * 代表该任务曾经被启动,但当前处于暂停状态
     */
    PAUSE,
    /**
     * 代表该任务从未被启动
     */
    SHUTDOWN

}
