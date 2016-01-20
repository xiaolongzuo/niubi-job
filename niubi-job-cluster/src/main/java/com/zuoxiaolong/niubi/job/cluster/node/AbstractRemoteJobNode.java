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

package com.zuoxiaolong.niubi.job.cluster.node;

import com.zuoxiaolong.niubi.job.cluster.launcher.Bootstrap;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoader;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 01:19
 */
public abstract class AbstractRemoteJobNode extends AbstractNode implements RemoteJobNode {

    private ReentrantLock lock = new ReentrantLock();

    private Map<String, Container> containerCache;

    public AbstractRemoteJobNode() {
        super(Bootstrap.properties());
        this.containerCache = new ConcurrentHashMap<>();
    }

    protected Map<String, Container> getContainerCache() {
        return Collections.unmodifiableMap(containerCache);
    }

    public Container getContainer(String jarFileName, String packagesToScan, boolean isSpring) {
        Container container = containerCache.get(jarFileName);
        if (container != null) {
            return container;
        }
        lock.lock();
        try {
            container = containerCache.get(jarFileName);
            if (container == null) {
                try {
                    container = createContainer(jarFileName, packagesToScan, isSpring);
                } catch (Exception e) {
                    LoggerHelper.error("create container for " + jarFileName + " failed.", e);
                    throw new NiubiException(e);
                }
                containerCache.put(jarFileName, container);
            }
            return container;
        } finally {
            lock.unlock();
        }
    }

    public Container createContainer(String jarFileName, String packagesToScan, boolean isSpring) throws Exception {
        String jarFilePath = JarFileHelper.downloadJarFile(Bootstrap.getJobDir(), Bootstrap.getJarUrl(jarFileName));
        String containerClassName;
        if (isSpring) {
            containerClassName = "com.zuoxiaolong.niubi.job.spring.container.DefaultSpringContainer";
        } else {
            containerClassName = "com.zuoxiaolong.niubi.job.scheduler.container.DefaultContainer";
        }
        ApplicationClassLoader applicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        Class<? extends Container> containerClass = (Class<? extends Container>) applicationClassLoader.loadClass(containerClassName);
        Class<?>[] parameterTypes = new Class[]{ClassLoader.class, Properties.class, String.class, String.class};
        Constructor<? extends Container> containerConstructor = containerClass.getConstructor(parameterTypes);
        return containerConstructor.newInstance(applicationClassLoader, Bootstrap.properties(), packagesToScan, jarFilePath);
    }

}
