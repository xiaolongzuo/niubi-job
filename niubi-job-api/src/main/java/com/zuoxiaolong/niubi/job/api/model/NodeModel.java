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

package com.zuoxiaolong.niubi.job.api.model;

import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.ChildData;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 22:11
 */
@Getter
@Setter
public class NodeModel {

    private String id;

    private NodeData data;

    public NodeModel(ChildData childData) {
        this.id = childData.getPath().substring(0, childData.getPath().lastIndexOf("/"));
        this.data = JsonHelper.fromJson(childData.getData(), NodeData.class);
    }

    @Setter
    @Getter
    public static class NodeData {

        private String name;

        private NodeStatus status;

    }

    public enum NodeStatus {

        RUNNING, STARTED

    }

}
