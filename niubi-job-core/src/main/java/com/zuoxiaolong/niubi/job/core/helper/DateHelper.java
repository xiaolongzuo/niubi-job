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

package com.zuoxiaolong.niubi.job.core.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期帮助类.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface DateHelper {

    /**
     * 格式化日期为标准的日期字符串,{@code "yyyy-MM-dd HH:mm:ss"}
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    static String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

}
