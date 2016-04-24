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

import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Setter
@Getter
public class MasterNodeView extends AbstractNodeView {

    private List<String> jobPaths;

    public String getJobPathsHtmlString() {
        if (ListHelper.isEmpty(jobPaths)) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String jobPath : jobPaths) {
            stringBuffer.append(PathHelper.getEndPath(jobPath)).append("<br/>");
        }
        return stringBuffer.substring(0, stringBuffer.lastIndexOf("<br/>"));
    }

    public String getStateLabelClass() {
        if ("Master".equals(getNodeState())) {
            return "label-important";
        }
        if ("Slave".equals(getNodeState())) {
            return "label-info";
        }
        return "";
    }

}
