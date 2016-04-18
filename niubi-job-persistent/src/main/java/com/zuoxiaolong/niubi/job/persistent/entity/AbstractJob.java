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
public class AbstractJob extends AbstractEntity {

    private String groupName;

    private String jobName;

    private String jarFileName;

    private String packagesToScan;

    private String containerType;

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public void setPackagesToScan(String packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Column(name = "jar_file_name")
    public String getJarFileName() {
        return jarFileName;
    }

    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }

    @Column(name = "job_name")
    public String getJobName() {
        return jobName;
    }

    public String getPackagesToScan() {
        return packagesToScan;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    @Transient
    public String getModeLabelClass() {
        if ("Common".equals(containerType)) {
            return "label-Info";
        }
        if ("Spring".equals(containerType)) {
            return "label-success";
        }
        return "";
    }

}
