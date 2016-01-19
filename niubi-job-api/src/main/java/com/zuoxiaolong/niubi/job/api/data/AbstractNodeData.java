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

package com.zuoxiaolong.niubi.job.api.data;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 22:11
 */
public class AbstractNodeData<T extends AbstractNodeData> implements Comparable<T> {

    private String ip;

    private String state;

    private Integer runningJobCount = 0;

    public AbstractNodeData() {
    }

    public AbstractNodeData(String ip) {
        this.ip = ip;
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

    public void increment() {
        runningJobCount++;
    }

    public void decrement() {
        runningJobCount--;
    }

    @Override
    public int compareTo(AbstractNodeData data) {
        return data.getRunningJobCount() - this.runningJobCount;
    }

    @Override
    public String toString() {
        return "Data{" +
                "ip='" + ip + '\'' +
                ", state='" + state + '\'' +
                ", runningJobCount=" + runningJobCount +
                '}';
    }

}
