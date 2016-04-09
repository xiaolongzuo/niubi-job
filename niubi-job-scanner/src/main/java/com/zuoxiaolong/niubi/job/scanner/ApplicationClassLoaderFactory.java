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

package com.zuoxiaolong.niubi.job.scanner;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用的类加载器工厂,负责管理节点类加载器和jar包类加载器
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class ApplicationClassLoaderFactory {

    private static ClassLoader systemClassLoader;

    private static ApplicationClassLoader nodeApplicationClassLoader;

    private static Map<String, ApplicationClassLoader> jarApplicationClassLoaderCache = new HashMap<>();

    /**
     * 该工厂在使用之前,必须制定系统加载器
     *
     * @param systemClassLoader 系统加载器
     */
    public synchronized static void setSystemClassLoader(ClassLoader systemClassLoader) {
        ApplicationClassLoaderFactory.systemClassLoader = systemClassLoader;
    }

    /**
     * 获取节点的类加载器,对于一个节点来说,该类加载器唯一
     * 注意:由于niubi-job的启动类和lib在一个目录下,因此对于node级别的类加载器,不需要指定lib目录为该类加载器的URL.
     *
     * @return 节点的类加载器
     */
    public synchronized static ApplicationClassLoader getNodeApplicationClassLoader() {
        if (nodeApplicationClassLoader != null) {
            return nodeApplicationClassLoader;
        }
        if (systemClassLoader == null) {
            throw new IllegalStateException("Can't create nodeClassLoader because systemClassLoader is null.");
        }
        nodeApplicationClassLoader = new ApplicationClassLoader(systemClassLoader, true);
        return nodeApplicationClassLoader;
    }

    /**
     * 获取jar包对应的类加载器,对于一个固定的jar包来说,该类加载器唯一
     *
     * @param jarFilePath jar包的本地文件路径
     * @return jar包对应的类加载器
     */
    public static ApplicationClassLoader getJarApplicationClassLoader(String jarFilePath) {
        ApplicationClassLoader jarApplicationClassLoader = jarApplicationClassLoaderCache.get(jarFilePath);
        if (jarApplicationClassLoader != null) {
            return jarApplicationClassLoader;
        }
        synchronized (jarApplicationClassLoaderCache) {
            jarApplicationClassLoader = jarApplicationClassLoaderCache.get(jarFilePath);
            if (jarApplicationClassLoader != null) {
                return jarApplicationClassLoader;
            }
            jarApplicationClassLoader = new ApplicationClassLoader(nodeApplicationClassLoader, false);
            jarApplicationClassLoader.addJarFiles(jarFilePath);
            jarApplicationClassLoaderCache.put(jarFilePath, jarApplicationClassLoader);
            return jarApplicationClassLoader;
        }
    }

    /**
     * 创建一个普通的applicationClassLoader
     *
     * @param parent 父加载器
     * @param jarFilePaths jar包路径
     * @return 创建好的applicationClassLoader
     */
    public static ApplicationClassLoader createNormalApplicationClassLoader(ClassLoader parent, String... jarFilePaths){
        ApplicationClassLoader classLoader = new ApplicationClassLoader(parent, true);
        classLoader.addJarFiles(jarFilePaths);
        return classLoader;
    }

}
