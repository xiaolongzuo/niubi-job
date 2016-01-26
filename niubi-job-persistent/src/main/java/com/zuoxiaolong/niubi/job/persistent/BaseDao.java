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

	public <T> String save(T entity);

	public <T> T merge(T entity);

	public <T> void persist(T entity);

	public <T> void update(T entity);

	public <T> void delete(T entity);

	public <T> List<String> save(List<T> entityList);

	public <T> void update(List<T> entityList);

	public <T> void delete(List<T> entityList);

	public <T> List<T> getAll(Class<T> clazz);

	public <T> T get(Class<T> clazz, String id);

	public <T> T load(Class<T> clazz, String id);

	public <T> List<T> getList(Class<T> clazz, T entity);

	public <T> List<T> getList(Class<T> clazz, T entity, boolean useLike);

	public <T> T getUnique(Class<T> clazz, T entity);

	public <T> T getUnique(Class<T> clazz, T entity, boolean useLike);

	public <T extends AbstractEntity> Pager<T> getByPager(Class<T> clazz, Pager<T> pager);

	public <T extends AbstractEntity> Pager<T> getByPager(Class<T> clazz, Pager<T> pager, T entity);

	public <T extends AbstractEntity> Pager<T> getByPager(Class<T> clazz, Pager<T> pager, T entity, boolean useLike);

}
