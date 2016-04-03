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

    public synchronized static void setSystemClassLoader(ClassLoader systemClassLoader) {
        ApplicationClassLoaderFactory.systemClassLoader = systemClassLoader;
    }

    /**
     * 获取节点的类加载器,对于一个节点来说,该类加载器唯一
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

    public static ApplicationClassLoader createNormalApplicationClassLoader(ClassLoader parent, String... jarFilePaths){
        ApplicationClassLoader classLoader = new ApplicationClassLoader(parent, true);
        classLoader.addJarFiles(jarFilePaths);
        return classLoader;
    }

}
