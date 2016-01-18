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


package com.zuoxiaolong.niubi.job.api.data;

import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 14:46
 */
@Setter
@Getter
public abstract class AbstractJobData<T extends AbstractJobData> implements Comparable<T> {

    private String groupName;

    private String jobName;

    private String jarFileName;

    private String packagesToScan;

    private String cron;

    private String mode = "Common";

    private String state = "Shutdown";

    private String misfirePolicy = "None";

    private String jobOperationLogId;

    private String operationResult;

    private String errorMessage;

    private String originalJarFileName;

    private String operation;

    @Override
    public int compareTo(AbstractJobData data) {
        return (groupName + "." + jobName).compareTo(data.getGroupName() + "." + data.getJobName());
    }

    public void prepareOperation() {
        this.operationResult = "Waiting";
        this.errorMessage = null;
    }

    public void clearOperationLog() {
        this.jobOperationLogId = null;
        this.operationResult = null;
        this.errorMessage = null;
    }

    public boolean isOperated() {
        return StringHelper.isEmpty(this.operation) && StringHelper.isEmpty(this.originalJarFileName)
                && this.operationResult != null && !this.operationResult.equals("Waiting")
                && !StringHelper.isEmpty(this.jobOperationLogId);
    }

    public void operateSuccess() {
        this.operationResult = "Success";
        this.operation = null;
        this.originalJarFileName = null;
    }

    public void operateFailed(String errorMessage) {
        this.operationResult = "Failed";
        this.errorMessage = errorMessage;
        this.operation = null;
        this.originalJarFileName = null;
    }

    public boolean isSpring() {
        return mode != null && mode.equals("Spring");
    }

    public boolean isStart() {
        return operation != null && operation.equals("Start");
    }

    public boolean isRestart() {
        return operation != null && operation.equals("Restart");
    }

    public boolean isPause() {
        return operation != null && operation.equals("Pause");
    }

    public boolean isUnknownOperation() {
        return !isStart() && !isRestart() && !isPause();
    }

    @Override
    public String toString() {
        return "Data{" +
                "groupName='" + groupName + '\'' +
                ", jobName='" + jobName + '\'' +
                ", originalJarFileName='" + originalJarFileName + '\'' +
                ", jarFileName='" + jarFileName + '\'' +
                ", packagesToScan='" + packagesToScan + '\'' +
                ", operation='" + operation + '\'' +
                ", cron='" + cron + '\'' +
                ", mode='" + mode + '\'' +
                ", state='" + state + '\'' +
                ", misfirePolicy='" + misfirePolicy + '\'' +
                '}';
    }

}
