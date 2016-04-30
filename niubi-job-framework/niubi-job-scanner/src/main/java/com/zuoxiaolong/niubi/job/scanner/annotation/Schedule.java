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

package com.zuoxiaolong.niubi.job.scanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 给方法添加该注解,代表该方法是一个可以被调度的方法.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {

    /**
     * cron表达式.如果是非集群环境,则niubi-job将会取该值作为任务的cron表达式.
     * 如果是集群环境,则该值将被忽略,使用用户在console控制台传递的cron表达式为准.
     *
     * @return cron
     */
    String cron() default "";

    /**
     * 错过的任务策略.如果是非集群环境,则niubi-job将会取该值作为错过的任务策略.
     * 如果是集群环境,则该值将被忽略,使用用户在console控制台传递的策略为准.
     *
     * @return misfirePolicy
     */
    MisfirePolicy misfirePolicy() default MisfirePolicy.None;

}
