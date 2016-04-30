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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface ReflectHelper {

    static void copyFieldValues(Object source, Object target) {
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

    static void copyFieldValuesSkipNull(Object source, Object target) {
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

    static Field[] getAllFields(Object object) {
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

    static Method getGetterMethod(Class<?> clazz, String fieldName) {
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return getInheritMethod(clazz, methodName, new Class<?>[]{});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static Method getGetterMethod(Class<?> clazz, Field field) {
        return getGetterMethod(clazz, field.getName());
    }

    static Method getSetterMethod(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return getInheritMethod(clazz, methodName, new Class<?>[]{field.getType()});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static Method getInheritMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
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

    static Object getFieldValueWithGetterMethod(Object object, Class<?> clazz, String fieldName) {
        Method method = getGetterMethod(clazz, fieldName);
        try {
            return method.invoke(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static Object setFieldValueWithSetterMethod(Object target, Object value, Class<?> clazz, Field field) {
        Method method = getSetterMethod(clazz, field);
        try {
            return method.invoke(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
