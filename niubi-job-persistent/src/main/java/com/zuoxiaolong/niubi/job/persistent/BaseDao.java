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

package com.zuoxiaolong.niubi.job.persistent;

import com.zuoxiaolong.niubi.job.persistent.entity.AbstractEntity;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface BaseDao {

	<T> String save(T entity);

	<T> void update(T entity);

	<T> void delete(T entity);

	<T> List<String> save(List<T> entityList);

	<T> void update(List<T> entityList);

	<T> void delete(List<T> entityList);

	<T> List<T> getAll(Class<T> clazz);

	<T> T get(Class<T> clazz, String id);

	<T> List<T> getList(Class<T> clazz, T entity);

	<T> List<T> getList(Class<T> clazz, T entity, boolean useLike);

	<T> T getUnique(Class<T> clazz, T entity);

	<T> T getUnique(Class<T> clazz, T entity, boolean useLike);

	<T extends AbstractEntity> Pager<T> getByPager(Class<T> clazz, Pager<T> pager, T entity, boolean useLike);

}
