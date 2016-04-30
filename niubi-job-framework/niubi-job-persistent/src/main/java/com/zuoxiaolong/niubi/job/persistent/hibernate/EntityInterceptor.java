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

package com.zuoxiaolong.niubi.job.persistent.hibernate;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component
public class EntityInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = -9150947520528806723L;

	private static final String CREATE_DATE = "createDate";
	private static final String MODIFY_DATE = "modifyDate";

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		Date createDate = new Date();
		for (int i = 0; i < propertyNames.length; i++) {
			if (CREATE_DATE.equals(propertyNames[i]) || MODIFY_DATE.equals(propertyNames[i])) {
				state[i] = createDate;
			}
		}
		return true;
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		Date updateDate = new Date();
		for (int i = 0; i < propertyNames.length; i++) {
			if (MODIFY_DATE.equals(propertyNames[i])) {
				currentState[i] = updateDate;
			}
		}
		return true;
	}

}
