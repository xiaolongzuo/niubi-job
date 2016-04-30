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

import com.zuoxiaolong.niubi.job.core.helper.ObjectHelper;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.persistent.entity.AbstractEntity;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component("baseDao")
public class BaseDaoImpl implements BaseDao {

    @Autowired
	private SessionFactory sessionFactory;

    @Override
	public <T> String save(T entity) {
		return (String) getHibernateSession().save(entity);
	}

	@Override
	public <T> void update(T entity) {
		getHibernateSession().update(entity);
	}

	@Override
	public <T> void delete(T entity) {
		getHibernateSession().delete(entity);
	}

	@Override
	public <T> List<String> save(List<T> entityList) {
		List<String> idList = new ArrayList<>();
		Session session = getHibernateSession();
        idList.addAll(entityList.stream().map(entity -> (String) session.save(entity)).collect(Collectors.toList()));
		return idList;
	}

	@Override
	public <T> void update(List<T> entityList) {
		Session session = getHibernateSession();
        entityList.forEach(session::update);
	}

	@Override
	public <T> void delete(List<T> entityList) {
		Session session = getHibernateSession();
        entityList.forEach(session::delete);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getAll(Class<T> clazz) {
		Query query = getHibernateSession().createQuery("from " + getEntityAnnotationName(clazz));
		return query.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz, String id) {
		return (T) getHibernateSession().get(clazz, id);
	}

	@Override
	public <T> List<T> getList(Class<T> clazz, T entity) {
		return getList(clazz, entity, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getList(Class<T> clazz, T entity, boolean useLike) {
		StringBuffer sqlBuffer = new StringBuffer("from " + getEntityAnnotationName(clazz) + " where 1=1 ");

		List<Object> valueList = generateValueListAndSetSql(clazz, entity, sqlBuffer, useLike);

		String querySql = sqlBuffer.toString() + " order by createDate desc";
		Query query = getHibernateSession().createQuery(querySql);
		setParameters(query, valueList);

		return query.list();
	}

	@Override
	public <T> T getUnique(Class<T> clazz, T entity) {
		return getUnique(clazz, entity, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getUnique(Class<T> clazz, T entity, boolean useLike) {
		StringBuffer sqlBuffer = new StringBuffer("from " + getEntityAnnotationName(clazz) + " where 1=1 ");

		List<Object> valueList = generateValueListAndSetSql(clazz, entity, sqlBuffer, useLike);

		String querySql = sqlBuffer.toString();
		Query query = getHibernateSession().createQuery(querySql);
		setParameters(query, valueList);

		return (T) query.uniqueResult();
	}

	@Override
	public <T extends AbstractEntity> Pager<T> getByPager(Class<T> clazz, Pager<T> pager, T entity, boolean useLike) {
		if (pager == null) {
			pager = new Pager<>();
		}

		StringBuffer sqlBuffer = new StringBuffer("from " + getEntityAnnotationName(clazz) + " where 1=1 ");

		List<Object> valueList = generateValueListAndSetSql(clazz, entity, sqlBuffer, useLike);

		pager.setDataList(getDataList(valueList, sqlBuffer, pager));

		pager.setTotalCount(getTotalCount(sqlBuffer, valueList));

		return pager;
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractEntity> List<T> getDataList(List<Object> valueList, StringBuffer sqlBuffer, Pager<T> pager) {
		String querySql = sqlBuffer.toString() + " order by createDate desc";
		Query query = getHibernateSession().createQuery(querySql);
		setParameters(query, valueList);
		query.setFirstResult(pager.getFirstIndex());
		query.setMaxResults(pager.getPageSize());
		return query.list();
	}

	private <T extends AbstractEntity> int getTotalCount(StringBuffer sqlBuffer, List<Object> valueList) {
		sqlBuffer.insert(0, "select count(id) ");
		Query query = getHibernateSession().createQuery(sqlBuffer.toString());
		setParameters(query, valueList);
		return ((Long) query.uniqueResult()).intValue();
	}

	private <T> void setParameters(Query query, List<Object> valueList) {
		for (int i = 0; i < valueList.size(); i++) {
			query.setParameter(String.valueOf(i), valueList.get(i));
		}
	}

	private <T> List<Object> generateValueListAndSetSql(Class<T> clazz, T entity, StringBuffer sqlBuffer, boolean useLike) {
		List<Object> valueList = new ArrayList<>();
		if (entity != null) {
			Field[] fields = ReflectHelper.getAllFields(entity);
			for (int i = 0, index = 0; i < fields.length; i++) {
				Field field = fields[i];
				int modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || Modifier.isFinal(modifiers) || ObjectHelper.isTransientId(clazz, field)) {
					continue;
				}
				Object value = ReflectHelper.getFieldValueWithGetterMethod(entity, entity.getClass(), field.getName());
				if (ObjectHelper.isEmpty(value)) {
					continue;
				}
				if (field.getType() == String.class && useLike) {
					sqlBuffer.append("and " + field.getName() + " like ?" + index++ + " ");
					valueList.add("%" + value + "%");
				} else {
					sqlBuffer.append("and " + field.getName() + "=?" + index++ + " ");
					valueList.add(value);
				}
			}
		}
		return valueList;
	}

	private Session getHibernateSession() {
		try {
			return sessionFactory.getCurrentSession();
		} catch (Exception ignored) {
			return sessionFactory.openSession();
		}
	}

    public String getEntityAnnotationName(Class<?> clazz) {
        try {
            Entity entityAnnotation = clazz.getAnnotation(Entity.class);
            return StringHelper.isEmpty(entityAnnotation.name()) ? clazz.getSimpleName() : entityAnnotation.name();
        } catch (Exception e) {
            return clazz.getSimpleName();
        }
    }

}
