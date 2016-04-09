package com.zuoxiaolong.niubi.job.scanner.job;

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

import java.util.HashMap;

/**
 * 任务参数,用于支持使用参数启动任务.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class JobParameter extends HashMap<String, Object> {

    public Integer getInteger(String key) {
        return Integer.valueOf(get(key).toString());
    }

    public Long getLong(String key) {
        return Long.valueOf(get(key).toString());
    }

    public String getString(String key) {
        return get(key).toString();
    }

}
