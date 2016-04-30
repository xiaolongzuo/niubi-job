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

import com.zuoxiaolong.niubi.job.core.exception.ConfigException;
import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 唯一的job扫描器实现.
 * 该实现同时实现了扫描classpath和jar包的功能
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class LocalAndRemoteJobScanner extends AbstractJobScanner {

    /**
     * 扫描器唯一的构造方法,将使用指定的类加载器去扫描
     *
     * @param classLoader 扫描器所使用的类加载器
     * @param packagesToScan 将被扫描的package
     * @param containsClasspath 是否要扫描classpath路径下的类
     * @param jarFilePaths 需要扫描的jar包
     */
    LocalAndRemoteJobScanner(ClassLoader classLoader, String packagesToScan, boolean containsClasspath, String... jarFilePaths) {
        super(classLoader, packagesToScan, jarFilePaths);
        if (containsClasspath) {
            scanClasspath();
        }
        scanJarFiles();
    }

    protected void scanClasspath() {
        URL url = getClassLoader().getResource("");
        if (url == null) {
            LoggerHelper.error("classpath can't be find.");
            throw new ConfigException();
        }
        if (url.getProtocol().toLowerCase().equals("file")) {
            LoggerHelper.info("scan classpath [" + url + "]");
            File[] children = new File(url.getFile()).listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    fill("", child);
                }
            }
        } else {
            LoggerHelper.warn("url [" + url + "] is not a file but a " + url.getProtocol() + ".");
        }
    }

    protected void scanJarFiles() {
        for (String jarFilePath : getJarFilePaths()) {
            File file = new File(jarFilePath);
            if (file.exists()) {
                scanJarFile(jarFilePath);
            } else {
                LoggerHelper.warn("jar file [" + jarFilePath + "] can't be found.");
            }
        }
    }

    private void fill(String packageName, File file) {
        String fileName = file.getName();
        if (file.isFile() && fileName.endsWith(".class")) {
            String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf("."));
            super.scanClass(className);
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    if (StringHelper.isEmpty(packageName)) {
                        fill(fileName, child);
                    } else {
                        fill(packageName + "." + fileName, child);
                    }
                }
            }
        }
    }

    private void scanJarFile(String jarFilePath) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(jarFilePath);
        } catch (Throwable e) {
            LoggerHelper.warn("get jar file failed. [" + jarFilePath +"]");
            return;
        }
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            String jarEntryName = jarEntryEnumeration.nextElement().getName();
            if (jarEntryName != null && jarEntryName.equals(APPLICATION_CONTEXT_XML_PATH)) {
                setHasSpringEnvironment(true);
                LoggerHelper.info("find spring config file [" + APPLICATION_CONTEXT_XML_PATH + "] .");
                continue;
            }
            if (jarEntryName == null || !jarEntryName.endsWith(".class")) {
                continue;
            }
            String className = ClassHelper.getClassName(jarEntryName);
            super.scanClass(className);
        }
    }

}
