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

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/13 23:20
 */
@Setter
@Getter
public class JobJarModel extends ChildDataModel<JobJarModel.JobJarData> {

    public JobJarModel(ChildData childData) {
        super(childData);
    }

    public JobJarModel(String path, byte[] bytes) {
        super(path, bytes);
    }

    @Setter
    @Getter
    public static class JobJarData {

        private Mode mode;

        private List<String> jobs;

        public boolean isSpring() {
            return mode == Mode.SPRING;
        }

    }

    public enum Mode {
        SPRING, COMMON
    }

}
