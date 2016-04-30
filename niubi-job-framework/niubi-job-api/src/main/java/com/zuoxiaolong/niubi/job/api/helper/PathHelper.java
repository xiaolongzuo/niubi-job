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

package com.zuoxiaolong.niubi.job.api.helper;

import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;

/**
 * path帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface PathHelper {

    static String getParentPath(String path) {
        AssertHelper.notNull(path, "path can't be null.");
        int index = path.lastIndexOf("/");
        if (index < 0) {
            return path;
        }
        return path.substring(0, index);
    }

    static String getJobPath(String jobParentPath, String group, String name) {
        AssertHelper.notNull(jobParentPath, "jobParentPath can't be null.");
        AssertHelper.notNull(group, "group can't be null.");
        AssertHelper.notNull(name, "name can't be null.");
        return jobParentPath + "/" + group + "." + name;
    }

    static String getEndPath(String path) {
        AssertHelper.notNull(path, "path can't be null.");
        return path.indexOf("/") >= 0 ? path.substring(path.lastIndexOf("/") + 1) : path;
    }

}
