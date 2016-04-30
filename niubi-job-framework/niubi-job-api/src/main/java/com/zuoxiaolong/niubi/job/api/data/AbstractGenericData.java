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

package com.zuoxiaolong.niubi.job.api.data;

import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.core.exception.UnknownGenericTypeException;
import com.zuoxiaolong.niubi.job.core.helper.JsonHelper;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ZK数据节点抽象的通用类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractGenericData<E extends AbstractGenericData, T extends Comparable<T>> extends AbstractData implements Comparable<E> {

    private T data;

    public AbstractGenericData(ChildData childData) {
        this(childData.getPath(), childData.getData());
    }

    public AbstractGenericData(String path, byte[] bytes) {
        this.path = path;
        this.id = PathHelper.getEndPath(this.path);
        this.data = JsonHelper.fromJson(bytes, getGenericType());
    }

    public AbstractGenericData(String path, T data) {
        this.path = path;
        this.id = PathHelper.getEndPath(this.path);
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private Class<T> getGenericType() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<T>) parameterizedType.getActualTypeArguments()[1];
        }
        throw new UnknownGenericTypeException();
    }

    public byte[] getDataBytes() {
        return JsonHelper.toBytes(data);
    }

    @Override
    public int compareTo(E e) {
        return data.compareTo((T) e.getData());
    }

    @Override
    public String toString() {
        return "GenericData{" +
                "data=" + JsonHelper.toJson(data) +
                '}';
    }

}
