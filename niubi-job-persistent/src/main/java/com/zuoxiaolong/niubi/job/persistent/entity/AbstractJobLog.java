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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@MappedSuperclass
public class AbstractJobLog extends AbstractEntity {

    private String groupName;

    private String jobName;

    private String jobOperation;

    private String originalJarFileName;

    private String jobCron;

    private String jarFileName;

    private String containerType;

    private String misfirePolicy;

    private String operationResult = "Waiting";

    private String errorMessage;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJobOperation(String jobOperation) {
        this.jobOperation = jobOperation;
    }

    public void setOriginalJarFileName(String originalJarFileName) {
        this.originalJarFileName = originalJarFileName;
    }

    public void setJobCron(String jobCron) {
        this.jobCron = jobCron;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public void setMisfirePolicy(String misfirePolicy) {
        this.misfirePolicy = misfirePolicy;
    }

    public void setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getJobName() {
        return jobName;
    }

    @Column(length = 30)
    public String getJobOperation() {
        return jobOperation;
    }

    public String getOriginalJarFileName() {
        return originalJarFileName;
    }

    @Column(length = 30)
    public String getJobCron() {
        return jobCron;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    @Column(length = 30)
    public String getContainerType() {
        return containerType;
    }

    @Column(length = 30)
    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    @Column(length = 30)
    public String getOperationResult() {
        return operationResult;
    }

    @Column(length = 5000)
    public String getErrorMessage() {
        return errorMessage;
    }

    @Transient
    public String getOperationLabelClass() {
        if ("Start".equals(jobOperation)) {
            return "label-warning";
        }
        if ("Restart".equals(jobOperation)) {
            return "label-success";
        }
        if ("Pause".equals(jobOperation)) {
            return "label-important";
        }
        return "";
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
