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
 * @author Xiaolong Zuo
 * @since 16/1/18 00:37
 */
public abstract class ApplicationClassLoaderFactory {

    private static ClassLoader systemClassLoader;

    private static ApplicationClassLoader nodeApplicationClassLoader;

    private static Map<String, ApplicationClassLoader> jarApplicationClassLoaderCache = new HashMap<>();

    public synchronized static void setSystemClassLoader(ClassLoader systemClassLoader) {
        ApplicationClassLoaderFactory.systemClassLoader = systemClassLoader;
    }

    public synchronized static ApplicationClassLoader getNodeApplicationClassLoader() {
        if (nodeApplicationClassLoader != null) {
            return nodeApplicationClassLoader;
        }
        if (systemClassLoader == null) {
            throw new IllegalStateException("Can't create nodeClassLoader because systemClassLoader is null.");
        }
        nodeApplicationClassLoader = new ApplicationClassLoader(systemClassLoader);
        return nodeApplicationClassLoader;
    }

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
            jarApplicationClassLoader = new ApplicationClassLoader(nodeApplicationClassLoader);
            jarApplicationClassLoader.addJarFiles(jarFilePath);
            jarApplicationClassLoaderCache.put(jarFilePath, jarApplicationClassLoader);
            return jarApplicationClassLoader;
        }
    }

    public static ApplicationClassLoader createNormalApplicationClassLoader(ClassLoader parent, String... jarFilePaths){
        ApplicationClassLoader classLoader = new ApplicationClassLoader(parent);
        classLoader.addJarFiles(jarFilePaths);
        return classLoader;
    }

}
