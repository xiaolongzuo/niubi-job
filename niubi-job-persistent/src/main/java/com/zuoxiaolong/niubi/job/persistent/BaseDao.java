package com.zuoxiaolong.niubi.job.persistent;

import com.zuoxiaolong.niubi.job.persistent.entity.AbstractEntity;

import java.util.List;

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
