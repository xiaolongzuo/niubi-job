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

import javax.persistence.MappedSuperclass;

/**
 * @author Xiaolong Zuo
 * @since 16/1/19 01:31
 */
@MappedSuperclass
public class AbstractNode extends AbstractEntity {

    private String identifier;

    private String ip;

    private String state;

    private Integer runningJobCount;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getRunningJobCount() {
        return runningJobCount;
    }

    public void setRunningJobCount(Integer runningJobCount) {
        this.runningJobCount = runningJobCount;
    }

}