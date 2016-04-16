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

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 对象帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class ObjectHelper {

	private static final String TRANSIENT_ID_SUFFIX = "Id";

	public static boolean isEmpty(Object object) {
		return object == null || object.toString().trim().length() == 0;
	}

	public static <T> boolean isTransientId(Class<T> clazz, Field field) {
		if (!field.getName().endsWith(TRANSIENT_ID_SUFFIX)) {
			return false;
		}
		Method getMethod = ReflectHelper.getGetterMethod(clazz, field);
		if (getMethod != null && getMethod.getAnnotation(Transient.class) != null) {
			return true;
		}
		return false;
	}

}
