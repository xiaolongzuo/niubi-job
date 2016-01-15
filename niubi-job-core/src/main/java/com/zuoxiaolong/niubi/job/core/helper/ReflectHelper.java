package com.zuoxiaolong.niubi.job.core.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.*;

public abstract class ReflectHelper {

	private static final String GET_METHOD_PREFIX = "get";

	private static final String SET_METHOD_PREFIX = "set";

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
		if (!ArrayHelper.isEmpty(list)) {
			return list.get(0).getClass();
		}
		return null;
	}

	public static <T> T copyFieldValue(Class<T> clazz, T source, T target) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			try {
				Object value = getFieldValue(source, field);
				if (value != null && value.toString().trim().length() > 0) {
					field.set(target, value);
				}
			} catch (Exception exception) {
				LoggerHelper.warn("field set failed", exception);
			}
		}
		return target;
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

	public static Field getAllField(Class<?> clazz, String fieldName) {
		if (clazz == null) {
			return null;
		}
		Field result = null;
		try {
			result = clazz.getDeclaredField(fieldName);
			return result;
		} catch (NoSuchFieldException e) {
			while (clazz.getSuperclass() != null) {
				try {
					result = clazz.getSuperclass().getDeclaredField(fieldName);
					return result;
				} catch (NoSuchFieldException e1) {
					clazz = clazz.getSuperclass();
				}
			}
		}
		return null;
	}

	public static Field[] getAllFields(Class<?> clazz) {
		Field[] result = null;
		Field[] fields = clazz.getDeclaredFields();
		while (clazz.getSuperclass() != null) {
			Field[] tempFields = clazz.getSuperclass().getDeclaredFields();
			result = new Field[fields.length + tempFields.length];
			System.arraycopy(fields, 0, result, 0, fields.length);
			System.arraycopy(tempFields, 0, result, fields.length, tempFields.length);
			fields = result;
			clazz = clazz.getSuperclass();
		}
		if (result == null) {
			result = fields;
		}
		return result;
	}

	public static Field[] getAllFields(Object entity) {
		if (entity == null) {
			return new Field[0];
		}
		Class<?> clazz = entity instanceof Class ? (Class<?>) entity : entity.getClass();
		return getAllFields(clazz);
	}

	public static Set<Class<?>> findAllClasses(ClassLoader classLoader, String[] packages) {
		LoggerHelper.info("��ʼ����domainʵ����......");
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		try {
			for (int i = 0; i < packages.length; i++) {
				URL url = classLoader.getResource(packages[i].replace('.', '/'));
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					LoggerHelper.info("domainʵ�����ļ���·��[" + filePath + "]");
					File domainDir = new File(filePath);
					if (!domainDir.exists() || !domainDir.isDirectory()) {
						LoggerHelper.warn("δ�����κ�domainʵ����......");
						return classes;
					}
					File[] classFiles = domainDir.listFiles(new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.getName().endsWith(".class");
						}
					});
					int successCount = 0;
					int errorCount = 0;
					for (File classFile : classFiles) {
						LoggerHelper.info("��ʼ����[" + classFile.getAbsolutePath() + "]");
						String className = classFile.getName().substring(0, classFile.getName().length() - 6);
						try {
							classes.add(classLoader.loadClass(packages[i] + '.' + className));
							successCount++;
						} catch (ClassNotFoundException e) {
							LoggerHelper.warn("δ�ҵ�������[" + classFile.getAbsolutePath() + "]ʧ��......");
							errorCount++;
						}
					}
					LoggerHelper.info("������[" + successCount + "]��domainʵ���࣬ʧ�ܸ���[" + errorCount + "]......");
				}
			}
		} catch (IOException cause) {
			throw new RuntimeException(cause);
		}
		return classes;
	}

	public static Method getMethodByName(Class<?> clazz, String methodName) {
		Method[] methods = clazz.getDeclaredMethods();
		if (methods == null || methods.length == 0) {
			return null;
		}
		if (methodName == null || methodName.length() == 0) {
			return null;
		}
		List<Method> methodList = new ArrayList<Method>();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				methodList.add(method);
			}
		}
		if (methodList.size() == 0) {
			return null;
		}
		if (methodList.size() > 1) {
			return null;
		}
		return methodList.get(0);
	}

	public static <T> Method getGetMethod(Class<T> clazz, Field field) {
		if (field == null || clazz == null) {
			return null;
		}
		String upperName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
		try {
			return clazz.getMethod(GET_METHOD_PREFIX + upperName, new Class<?>[] {});
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public static <T> Method getSetMethod(Class<T> clazz, Field field) {
		if (field == null || clazz == null) {
			return null;
		}
		String upperName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
		try {
			return clazz.getMethod(SET_METHOD_PREFIX + upperName, new Class<?>[] {});
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T copyFieldsByName(T source, T target) {
		if (source == null) {
			return target;
		}
		Class<T> clazz = (Class<T>) source.getClass();
		if (target == null) {
			try {
				target = clazz.newInstance();
			} catch (Exception exception) {
				LoggerHelper.warn("copy failed", exception);
				return target;
			}
		}
		return ReflectHelper.copyFieldValue(clazz, source, target);
	}

	public static <T> Map<String, Object> beanToMap(Object bean) {
		Map<String, Object> map = new HashMap<String, Object>();
		Class<?> clazz = bean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (Modifier.isPrivate(fields[i].getModifiers()) && !Modifier.isStatic(fields[i].getModifiers()) && !Modifier.isFinal(fields[i].getModifiers())) {
				map.put(fields[i].getName(), ReflectHelper.getFieldValue(bean, fields[i]));
			}
		}
		return map;
	}

}
