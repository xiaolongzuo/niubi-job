package com.zuoxiaolong.niubi.job.core.helper;

/*
 * Copyright 2002-2015 the original author or authors.
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

/**
 * class帮助类,包含classLoader的帮助方法.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface ClassHelper {

    /**
     * 根据类名和方法名获取唯一的描述符
     *
     * @param className 类名
     * @param methodName 方法名
     * @return 唯一的描述符
     */
    static String getUniqueDescriptor(String className, String methodName) {
        AssertHelper.notEmpty(methodName, "className can't be null.");
        return StringHelper.isEmpty(className) ? methodName : (className + "." + methodName);
    }

    /**
     * 根据类名获取包名
     *
     * @param className 类名
     * @return 包名
     */
    static String getPackageName(String className) {
        AssertHelper.notEmpty(className, "className can't be null.");
        int index = className.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return className.substring(0, index);
    }

    /**
     * 根据jar包里的entry名称获取类名
     *
     * @param jarEntryName jar包中的entry名称
     * @return 类名,如果没找到则为{@code null}
     */
    static String getClassName(String jarEntryName) {
        if (jarEntryName.endsWith(".class")) {
            return jarEntryName.replace("/", ".").substring(0, jarEntryName.lastIndexOf("."));
        }
        return null;
    }

    /**
     * 覆盖当前线程的类加载器
     *
     * @param classLoaderToUse 要使用的类加载器
     * @return 返回线程在覆盖之前使用的类加载器
     */
    static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
            currentThread.setContextClassLoader(classLoaderToUse);
            return threadContextClassLoader;
        }
        else {
            return null;
        }
    }

    /**
     * 获取默认的类加载器,优先级为:
     * 1,当前线程的类加载器
     * 2,加载ClassHelper类的类加载器
     * 3,系统类加载器
     *
     * @return 获取默认的类加载器
     */
    static ClassLoader getDefaultClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (classLoader == null) {
            // No thread context class loader -> use class loader of this class.
            classLoader = ClassHelper.class.getClassLoader();
            if (classLoader == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    classLoader = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return classLoader;
    }

}
