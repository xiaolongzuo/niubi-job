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

/**
 * ZK Job数据节点抽象类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
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

    private String originalJarFileName;

    private String operation;

    private Long version;

    private String errorMessage;

    public void incrementVersion() {
        if (version == null || version == Long.MAX_VALUE) {
            this.version = 1L;
        } else {
            this.version++;
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String getPackagesToScan() {
        return packagesToScan;
    }

    public void setPackagesToScan(String packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMisfirePolicy() {
        return misfirePolicy;
    }

    public void setMisfirePolicy(String misfirePolicy) {
        this.misfirePolicy = misfirePolicy;
    }

    public String getJobOperationLogId() {
        return jobOperationLogId;
    }

    public void setJobOperationLogId(String jobOperationLogId) {
        this.jobOperationLogId = jobOperationLogId;
    }

    public String getOperationResult() {
        return operationResult;
    }

    public void setOperationResult(String operationResult) {
        this.operationResult = operationResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOriginalJarFileName() {
        return originalJarFileName;
    }

    public void setOriginalJarFileName(String originalJarFileName) {
        this.originalJarFileName = originalJarFileName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

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
                && this.operationResult != null && !this.operationResult.equals("Waiting");
    }

    public void init() {
        setState("Shutdown");
        setOperation(null);
        setOriginalJarFileName(null);
        setOperationResult("Success");
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

    public boolean isStartup() {
        return state != null && state.equals("Startup");
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
        return "JobData {" +
                "groupName='" + groupName + '\'' +
                ", jobName='" + jobName + '\'' +
                ", jarFileName='" + jarFileName + '\'' +
                ", packagesToScan='" + packagesToScan + '\'' +
                ", cron='" + cron + '\'' +
                ", mode='" + mode + '\'' +
                ", state='" + state + '\'' +
                ", misfirePolicy='" + misfirePolicy + '\'' +
                ", jobOperationLogId='" + jobOperationLogId + '\'' +
                ", operationResult='" + operationResult + '\'' +
                ", originalJarFileName='" + originalJarFileName + '\'' +
                ", operation='" + operation + '\'' +
                ", version=" + version +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
