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

package com.zuoxiaolong.niubi.job.cluster.startup;

import com.zuoxiaolong.niubi.job.core.exception.ConfigException;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoader;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Xiaolong Zuo
 * @since 16/1/20 23:24
 */
public class Bootstrap {

    private static final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    private static final ApplicationClassLoader applicationClassLoader;

    private static final String rootDir;

    private static final String confDir;

    private static final String libDir;

    private static final String jobDir;

    private static final Properties properties;

    private static final Object mutex = new Object();

    public static void main(String[] args) throws Exception {
        synchronized (mutex) {
            start();
            mutex.wait();
        }
    }

    static {
        rootDir = System.getProperty("user.dir");
        confDir = rootDir + "/conf";
        libDir = rootDir + "/lib";
        jobDir = rootDir + "/job";

        ApplicationClassLoaderFactory.setSystemClassLoader(systemClassLoader);

        applicationClassLoader = ApplicationClassLoaderFactory.getNodeApplicationClassLoader();

        Thread.currentThread().setContextClassLoader(applicationClassLoader);

        initApplicationClassLoader();

        properties = new Properties();

        loadProperties();
    }

    private static void initApplicationClassLoader() {
        File libFile = new File(libDir);
        if (!libFile.exists()) {
            throw new NiubiException(new IllegalArgumentException("can't find lib path."));
        }
        List<String> filePathList = new ArrayList<>();

        File[] jarFiles = libFile.listFiles();
        for (File jarFile : jarFiles) {
            if (jarFile.getName().endsWith(".jar")) {
                filePathList.add(jarFile.getAbsolutePath());
            }
        }
        applicationClassLoader.addFiles(filePathList.toArray());
    }

    private static void loadProperties() {
        try {
            File confFile = new File(confDir);
            if (!confFile.exists()) {
                throw new NiubiException(new IllegalArgumentException("can't find conf path."));
            }
            File[] propertiesFiles = confFile.listFiles();
            for (File propertiesFile : propertiesFiles) {
                if (propertiesFile.getName().endsWith(".properties")) {
                    properties.load(new FileInputStream(propertiesFile));
                }
            }
        } catch (IOException e) {
            throw new NiubiException(e);
        }
    }

    public static String getJarRepertoryUrl() {
        return StringHelper.appendSlant(properties.getProperty("jar.repertory.url", "http://localhost:8080/job"));
    }

    public static String getNodeMode() {
        return properties.getProperty("node.mode","masterSlave");
    }

    public static String getZookeeperAddresses() {
        return properties.getProperty("zookeeper.addresses","localhost:2181");
    }

    public static Properties properties() {
        return properties;
    }

    public static String getJarUrl(String jarFileName) {
        if ("masterSlave".equals(getNodeMode())) {
            return getJarRepertoryUrl() + "masterSlave/" + jarFileName;
        } else if ("standby".equals(getNodeMode())) {
            return getJarRepertoryUrl() + "standby/" + jarFileName;
        } else {
            throw new ConfigException();
        }
    }

    public static String getJobDir() {
        if ("masterSlave".equals(getNodeMode())) {
            return jobDir + "/masterSlave";
        } else if ("standby".equals(getNodeMode())) {
            return jobDir + "/standby";
        } else {
            throw new ConfigException();
        }
    }

    public static void start() throws Exception {
        String nodeClassName;
        if ("masterSlave".equals(getNodeMode())) {
            nodeClassName = "com.zuoxiaolong.niubi.job.cluster.node.MasterSlaveNode";
        } else if ("standby".equals(getNodeMode())) {
            nodeClassName = "com.zuoxiaolong.niubi.job.cluster.node.StandbyNode";
        } else {
            throw new ConfigException();
        }
        Class<?> nodeClass = applicationClassLoader.loadClass(nodeClassName);
        Constructor<?> nodeConstructor = nodeClass.getConstructor();
        Object nodeInstance = nodeConstructor.newInstance();
        Method joinMethod = nodeClass.getDeclaredMethod("join");
        joinMethod.invoke(nodeInstance);
    }

    public static void stop() throws Exception {
        String nodeClassName;
        if ("masterSlave".equals(getNodeMode())) {
            nodeClassName = "com.zuoxiaolong.niubi.job.cluster.node.MasterSlaveNode";
        } else if ("standby".equals(getNodeMode())) {
            nodeClassName = "com.zuoxiaolong.niubi.job.cluster.node.StandbyNode";
        } else {
            throw new ConfigException();
        }
        Class<?> nodeClass = applicationClassLoader.loadClass(nodeClassName);
        Constructor<?> nodeConstructor = nodeClass.getConstructor();
        Object nodeInstance = nodeConstructor.newInstance();
        Method exitMethod = nodeClass.getDeclaredMethod("exit");
        exitMethod.invoke(nodeInstance);
    }

}
