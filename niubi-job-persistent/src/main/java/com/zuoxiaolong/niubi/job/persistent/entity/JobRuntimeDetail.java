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

package com.zuoxiaolong.niubi.job.persistent.entity;

import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @author Xiaolong Zuo
 * @since 16/1/16 23:33
 */
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(uniqueConstraints = {@UniqueConstraint(name = "UNIQUE_JOB_RUNTIME_DETAIL", columnNames = {"group_name","job_name"})})
public class JobRuntimeDetail extends BaseEntity {

    private String groupName;

    private String jobName;

    private String jarFileName;

    private String packagesToScan;

    private String mode;

    private String state;

    private String cron;

    private String misfirePolicy;

    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }

    @Column(name = "job_name")
    public String getJobName() {
        return jobName;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public String getPackagesToScan() {
        return packagesToScan;
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

    public void setDefaultState() {
        this.state = "Shutdown";
    }

    private String originalJarFileName;

    private String operation;

    @Transient
    public String getOriginalJarFileName() {
        return originalJarFileName;
    }

    @Transient
    public String getOperation() {
        return operation;
    }

    @Transient
    public String getStateLabelClass() {
        if ("Shutdown".equals(state)) {
            return "label-warning";
        }
        if ("Startup".equals(state)) {
            return "label-success";
        }
        if ("Pause".equals(state)) {
            return "label-inverse";
        }
        return "";
    }

    @Transient
    public String getModeLabelClass() {
        if ("Common".equals(mode)) {
            return "label-Info";
        }
        if ("Spring".equals(mode)) {
            return "label-success";
        }
        return "";
    }

}
