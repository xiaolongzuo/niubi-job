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


package com.zuoxiaolong.niubi.job.persistent.entity;

import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 14:44
 */
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
public class Job extends BaseEntity {

    private String groupName;

    private String jobName;

    private JobJar jobJar;

    private String mode;

    private String state;

    private String cron;

    private String misfirePolicy;

    private String jarFileName;

    public String getGroupName() {
        return groupName;
    }

    public String getJobName() {
        return jobName;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public JobJar getJobJar() {
        return jobJar;
    }

    @Column(length = 30)
    public String getMode() {
        return mode;
    }

    @Column(length = 30)
    public String getState() {
        return state;
    }

    @Column(length = 30)
    public String getCron() {
        return cron;
    }

    @Column(length = 30)
    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    @Transient
    public String getJarFileName() {
        return jarFileName;
    }

}
