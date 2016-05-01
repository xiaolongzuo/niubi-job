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

import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.IOHelper;
import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * niubi-job的类加载器.
 * 当entrust为true时,该类加载完全遵循双亲委托模型,类加载器继承的顺序如下:
 *      bootstrap class loader
 *                ||
 *                ||
 *         ext class loader
 *                ||
 *                ||
 *        system class loader
 *                ||
 *                ||
 *      application class loader
 *
 * 当entrust为false时,该类加载完全遵循双亲委托模型,类加载器继承的顺序如下:
 *      bootstrap class loader
 *                ||
 *                ||
 *         ext class loader
 *                ||
 *                ||
 *        application class loader
 *                ||
 *                ||
 *        system class loader
 *
 * NOTE:如果是niubi-job本身的类,则无论entrust是否为true,都将优先由parent classloader加载.
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class ApplicationClassLoader extends URLClassLoader {

    private static final String NIUBI_JOB_PACKAGE_PREFIX = "com.zuoxiaolong.niubi.job";

    private Map<String, Class<?>> classMap = new HashMap<>();

    /**
     * 由于bootstrap级别的类加载器无法被java程序获取到,因此该类加载通常是ext级别的类加载器
     */
    private ClassLoader javaClassLoader;

    private boolean entrust;

    ApplicationClassLoader(ClassLoader parent, boolean entrust) {
        super(new URL[]{}, parent);
        AssertHelper.notNull(getParent(), "parent can't be null.");
        this.entrust = entrust;
        ClassLoader classLoader = String.class.getClassLoader();
        if (classLoader == null) {
            classLoader = getSystemClassLoader();
            while (classLoader.getParent() != null) {
                classLoader = classLoader.getParent();
            }
        }
        this.javaClassLoader = classLoader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = classMap.get(name);
        if (clazz != null) {
            return clazz;
        }
        synchronized (getClassLoadingLock(name)) {
            try {
                clazz = findLoadedClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (Throwable e) {
                //ignored
            }
            try {
                clazz = javaClassLoader.loadClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (Throwable e) {
                //ignored
            }
            try {
                if (entrust || isNiubiJobClass(name)) {
                    clazz = super.loadClass(name, resolve);
                    if (clazz != null) {
                        if (resolve) {
                            resolveClass(clazz);
                        }
                        return clazz;
                    }
                }
            } catch (Throwable e) {
                //ignored
            }
            try {
                InputStream resource = getResourceAsStream(binaryNameToPath(name, false));
                byte[] bytes = IOHelper.readStreamBytesAndClose(resource);
                clazz = defineClass(name, bytes, 0, bytes.length);
                if (clazz != null) {
                    classMap.put(name, clazz);
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (Throwable e) {
                //ignored
            }
            try {
                if (!entrust) {
                    clazz = super.loadClass(name, resolve);
                    if (clazz != null) {
                        if (resolve) {
                            resolveClass(clazz);
                        }
                        return clazz;
                    }
                }
            } catch (Throwable e) {
                //ignored
            }
            throw new ClassNotFoundException();
        }
    }

    private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
        // 1 for leading '/', 6 for ".class"
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        if (withLeadingSlash) {
            path.append('/');
        }
        path.append(binaryName.replace('.', '/'));
        path.append(".class");
        return path.toString();
    }

    @Override
    public URL getResource(String name) {
        URL url = javaClassLoader.getResource(name);
        if (url != null) {
            return url;
        }
        url = findResource(name);
        if (url != null) {
            return url;
        }
        return null;
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    protected boolean isNiubiJobClass(String className) {
        if (className != null && className.startsWith(NIUBI_JOB_PACKAGE_PREFIX)) {
            return true;
        }
        return false;
    }

    public synchronized void addFiles(Object... filePaths) {
        if (ListHelper.isEmpty(filePaths)) {
            return;
        }
        for (Object filePath : filePaths) {
            File file = new File(filePath.toString());
            if (file.exists()) {
                try {
                    addURL(file.toURI().toURL());
                } catch (Throwable e) {
                    LoggerHelper.warn("jar file [" + filePath + "] can't be add.");
                }
            } else {
                LoggerHelper.warn("jar file [" + filePath + "] can't be found.");
            }
        }
    }

    public synchronized void addJarFiles(String... jarFilePaths) {
        if (ListHelper.isEmpty(jarFilePaths)) {
            return;
        }
        for (String jarFilePath : jarFilePaths) {
            File file = new File(jarFilePath);
            if (file.exists()) {
                try {
                    addURL(file.toURI().toURL());
                } catch (Throwable e) {
                    LoggerHelper.warn("jar file [" + jarFilePath + "] can't be add.");
                }
            } else {
                LoggerHelper.warn("jar file [" + jarFilePath + "] can't be found.");
            }
        }
    }

}
