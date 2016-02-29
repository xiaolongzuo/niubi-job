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
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class ListHelper {

    public static <T> List<T> merge(List<T> list1, List<T> list2) {
        List<T> mergeResult = new ArrayList<>();
        if (list1 != null) {
            mergeResult.addAll(list1);
        }
        if (list2 != null) {
            mergeResult.addAll(list2);
        }
        return mergeResult;
    }

    public static <T> List<T> add(List<T> list, T element) {
        List<T> mergeResult = new ArrayList<>();
        if (list != null) {
            mergeResult.addAll(list);
        }
        if (element != null) {
            mergeResult.add(element);
        }
        return mergeResult;
    }

    public static <T> List<T> sub(List<T> list, T element) {
        List<T> mergeResult = new ArrayList<>();
        if (list != null) {
            mergeResult.addAll(list);
        }
        if (element != null) {
            mergeResult.remove(element);
        }
        return mergeResult;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static String[] listToArray(List<String> list) {
        if (list == null)
            return null;
        String[] array = new String[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

}
