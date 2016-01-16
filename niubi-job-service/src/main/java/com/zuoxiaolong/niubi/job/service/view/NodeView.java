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


package com.zuoxiaolong.niubi.job.service.view;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:02
 */
@Setter
@Getter
public class NodeView {

    private String id;

    private String ip;

    private String state;

    private Integer runningJobCount;

    public String getStateLabelClass() {
        if ("Master".equals(state)) {
            return "label-important";
        }
        if ("Backup".equals(state)) {
            return "label-info";
        }
        return "";
    }

}
