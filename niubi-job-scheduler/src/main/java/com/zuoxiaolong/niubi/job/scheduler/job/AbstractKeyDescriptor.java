package com.zuoxiaolong.niubi.job.scheduler.job;

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

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * @author Xiaolong Zuo
 * @since 1/12/2016 17:45
 */
public abstract class AbstractKeyDescriptor implements KeyDescriptor {

    private String group;

    private String name;

    public AbstractKeyDescriptor(String group, String name) {
        this.group = group;
        this.name = name;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TriggerKey triggerKey() {
        return TriggerKey.triggerKey(name, group);
    }

    @Override
    public JobKey jobKey() {
        return JobKey.jobKey(name, group);
    }

}
