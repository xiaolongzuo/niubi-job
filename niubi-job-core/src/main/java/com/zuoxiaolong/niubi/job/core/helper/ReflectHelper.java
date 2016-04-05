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

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface ReflectHelper {

    public static void copyFieldValues(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        Field[] fields = getAllFields(targetClass);
        for (int i = 0; i < fields.length; i++) {
            Field targetField = fields[i];
            if (targetField.isSynthetic()) {
                continue;
            }
            String fieldName = targetField.getName();
            try {
                Object sourceValue = getFieldValueWithGetterMethod(source, sourceClass, fieldName);
                setFieldValueWithSetterMethod(target, sourceValue, targetClass, targetField);
            } catch (Exception e) {
                //ignored
            }
        }
    }

    public static void copyFieldValuesSkipNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        Field[] fields = getAllFields(targetClass);
        for (int i = 0; i < fields.length; i++) {
            Field targetField = fields[i];
            if (targetField.isSynthetic()) {
                continue;
            }
            String fieldName = targetField.getName();
            try {
                Object sourceValue = getFieldValueWithGetterMethod(source, sourceClass, fieldName);
                if (sourceValue != null) {
                    setFieldValueWithSetterMethod(target, sourceValue, targetClass, targetField);
                }
            } catch (Exception e) {
                //ignored
            }
        }
    }

    public static Field[] getAllFields(Object object) {
        Class<?> clazz;
        if (object instanceof Class) {
            clazz = (Class<?>) object;
        } else {
            clazz = object.getClass();
        }
        Field[] fields = clazz.getDeclaredFields();
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            Field[] copy = fields;
            Field[] superClassFields = superClass.getDeclaredFields();
            Field[] newFields = new Field[copy.length + superClassFields.length];
            System.arraycopy(copy, 0, newFields, 0, copy.length);
            System.arraycopy(superClassFields, 0, newFields, copy.length, superClassFields.length);
            fields = newFields;
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    public static Method getGetterMethod(Class<?> clazz, String fieldName) {
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return getInheritMethod(clazz, methodName, new Class<?>[]{});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getGetterMethod(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return getInheritMethod(clazz, methodName, new Class<?>[]{});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getSetterMethod(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return getInheritMethod(clazz, methodName, new Class<?>[]{field.getType()});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getInheritMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return getInheritMethod(superClass, methodName, parameterTypes);
            } else {
                throw e;
            }
        }
    }

    public static Object getFieldValueWithGetterMethod(Object object, Class<?> clazz, Field field) {
        Method method = getGetterMethod(clazz, field);
        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldValueWithGetterMethod(Object object, Class<?> clazz, String fieldName) {
        Method method = getGetterMethod(clazz, fieldName);
        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object setFieldValueWithSetterMethod(Object target, Object value, Class<?> clazz, Field field) {
        Method method = getSetterMethod(clazz, field);
        try {
            return method.invoke(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean hasField(Class<?> clazz) {
		if (clazz.getDeclaredFields() == null || clazz.getDeclaredFields().length == 0) {
			return false;
		}
		return true;
	}

	public static Class<?> getParameterizedType(Class<?> clazz) {
		return getParameterizedType(clazz, 0);
	}

	public static Class<?> getParameterizedType(Class<?> clazz, int index) {
		Type type = clazz.getGenericSuperclass();
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return (Class<?>) parameterizedType.getActualTypeArguments()[index];
		}
		return null;
	}

	public static Class<?> getParameterizedType(Field field) {
		return getParameterizedType(field, 0);
	}

	public static Class<?> getParameterizedType(Field field, int index) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			return (Class<?>) parameterizedType.getActualTypeArguments()[index];
		}
		return null;
	}

	public static Class<?> getParameterizedType(List<?> list) {
		if (!ListHelper.isEmpty(list)) {
			return list.get(0).getClass();
		}
		return null;
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || clazz == String.class || clazz == Date.class || clazz == java.sql.Date.class || clazz == Timestamp.class || clazz == BigDecimal.class;
	}

	public static Object getFieldValue(Object entity, Field field) {
		field.setAccessible(true);
		Object value = null;
		try {
			value = field.get(entity);
		} catch (Exception exception) {
			LoggerHelper.warn("the field " + field.getName() + " will be ingored.");
		}
		return value;
	}

	public static Object getFieldValue(Object entity, String fieldName) {
		try {
			return getFieldValue(entity, entity.getClass().getDeclaredField(fieldName));
		} catch (Exception e) {
            LoggerHelper.error(fieldName, e);
			throw new RuntimeException(e);
		}
	}

	public static void setFieldValue(Object entity, Field field, Object value) {
		field.setAccessible(true);
		try {
			field.set(entity, value);
		} catch (Exception exception) {
            LoggerHelper.warn("the field " + field.getName() + " will be ingored.");
		}
	}

	public static void setFieldValue(Object entity, String fieldName, Object value) {
		try {
			setFieldValue(entity, entity.getClass().getDeclaredField(fieldName), value);
		} catch (Exception e) {
			LoggerHelper.error(fieldName, e);
			throw new RuntimeException(e);
		}
	}

	public static Method getMethodByName(Class<?> clazz, String methodName) {
		Method[] methods = clazz.getDeclaredMethods();
		if (methods == null || methods.length == 0) {
			return null;
		}
		if (methodName == null || methodName.length() == 0) {
			return null;
		}
		List<Method> methodList = new ArrayList<>();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				methodList.add(method);
			}
		}
		if (methodList.isEmpty()) {
			return null;
		}
		if (methodList.size() > 1) {
			return null;
		}
		return methodList.get(0);
	}

}
