package com.zuoxiaolong.niubi.job.core.helper;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class ObjectHelper {

	private static final String TRANSIENT_ID_SUFFIX = "Id";

	public static boolean isEmpty(Object object) {
		return object == null || object.toString().trim().length() == 0;
	}

	public static String getFieldNameForTransientId(Field field) {
		if (field == null) {
			throw new NullPointerException();
		}
		return field.getName().substring(0, field.getName().length() - TRANSIENT_ID_SUFFIX.length());
	}

	public static <T> boolean isTransientId(Class<T> clazz, Field field) {
		if (!field.getName().endsWith(TRANSIENT_ID_SUFFIX)) {
			return false;
		}
		Method getMethod = ReflectHelper.getGetMethod(clazz, field);
		if (getMethod != null && getMethod.getAnnotation(Transient.class) != null) {
			return true;
		}
		return false;
	}

}
