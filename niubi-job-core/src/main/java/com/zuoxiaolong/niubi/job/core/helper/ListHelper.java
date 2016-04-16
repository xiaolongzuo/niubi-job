package com.zuoxiaolong.niubi.job.core.helper;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 列表帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface ListHelper {

    static <T> List<T> add(List<T> list, T element) {
        List<T> mergeResult = new ArrayList<>();
        if (list != null) {
            mergeResult.addAll(list);
        }
        if (element != null) {
            mergeResult.add(element);
        }
        return mergeResult;
    }

    static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

}
