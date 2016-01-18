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

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 22:11
 */
@Getter
@Setter
public class NodeData extends GenericData<NodeData, NodeData.Data> {

    public NodeData(ChildData childData) {
        super(childData);
    }

    public NodeData(String path, byte[] bytes) {
        super(path, bytes);
    }

    public NodeData(String path, Data data) {
        super(path, data);
    }

    @Setter
    @Getter
    public static class Data implements Comparable<Data>{

        private String ip;

        private String state = "Backup";

        private Integer runningJobCount = 0;

        private List<String> jobPaths;

        public Data() {
        }

        public Data(String ip) {
            this.ip = ip;
        }

        @Override
        public int compareTo(Data data) {
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

}
