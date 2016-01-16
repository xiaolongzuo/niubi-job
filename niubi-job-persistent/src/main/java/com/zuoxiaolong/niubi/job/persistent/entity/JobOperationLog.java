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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Xiaolong Zuo
 * @since 16/1/17 04:47
 */
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
public class JobOperationLog extends BaseEntity {

    private String groupName;

    private String jobName;

    private String operation;

    private String originalJarFileName;

    private String cron;

    private String jarFileName;

    private String mode;

    private String misfirePolicy;

    private String operationResult = "Waiting";

    private String errorMessage;

    public String getGroupName() {
        return groupName;
    }

    public String getJobName() {
        return jobName;
    }

    @Column(length = 30)
    public String getOperation() {
        return operation;
    }

    public String getOriginalJarFileName() {
        return originalJarFileName;
    }

    @Column(length = 30)
    public String getCron() {
        return cron;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    @Column(length = 30)
    public String getMode() {
        return mode;
    }

    @Column(length = 30)
    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    @Column(length = 30)
    public String getOperationResult() {
        return operationResult;
    }

    @Column(length = 1000)
    public String getErrorMessage() {
        return errorMessage;
    }

    @Transient
    public String getOperationResultLabelClass() {
        if ("Waiting".equals(operationResult)) {
            return "label-warning";
        }
        if ("Success".equals(operationResult)) {
            return "label-success";
        }
        if ("Failed".equals(operationResult)) {
            return "label-important";
        }
        return "";
    }

}
