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

import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.cache.ChildData;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 14:46
 */
@Setter
@Getter
public class JobData extends GenericData<JobData.Data> {

    public JobData(ChildData childData) {
        super(childData);
    }

    public JobData(String path, byte[] bytes) {
        super(path, bytes);
    }

    @Setter
    @Getter
    public static class Data {

        private String jarFileName;

        private Mode mode;

        private State state;

        private String cron;

        private MisfirePolicy misfirePolicy;

        public boolean isSpring() {
            return mode == Mode.SPRING;
        }

    }

    public enum Mode {
        SPRING, COMMON
    }

    public enum State {
        STARTUP, PAUSE, SHUTDOWN
    }

    public enum MisfirePolicy {
        DoNothing, IgnoreMisfires, FireAndProceed, None
    }

}
