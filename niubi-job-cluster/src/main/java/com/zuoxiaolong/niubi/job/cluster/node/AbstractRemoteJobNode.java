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

import com.zuoxiaolong.niubi.job.cluster.startup.Bootstrap;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.JarFileHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.scanner.ApplicationClassLoaderFactory;
import com.zuoxiaolong.niubi.job.scheduler.container.Container;
import com.zuoxiaolong.niubi.job.scheduler.node.AbstractNode;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractRemoteJobNode extends AbstractNode implements RemoteJobNode {

    private Map<String, Container> containerCache;

    public AbstractRemoteJobNode() {
        super(Bootstrap.properties());
        this.containerCache = new ConcurrentHashMap<>();
    }

    protected Map<String, Container> getContainerCache() {
        return Collections.unmodifiableMap(containerCache);
    }

    protected void shutdownAllScheduler() {
        for (Container container : getContainerCache().values()) {
            container.schedulerManager().shutdown();
        }
    }

    public Container getContainer(String jarFileName, String packagesToScan, boolean isSpring) {
        String jarFilePath;
        try {
            jarFilePath = JarFileHelper.downloadJarFile(Bootstrap.getJobDir(), Bootstrap.getJarUrl(jarFileName));
        } catch (IOException e) {
            LoggerHelper.error("download jar file failed. [" + jarFileName + "]", e);
            throw new NiubiException(e);
        }
        Container container = containerCache.get(jarFileName);
        if (container != null) {
            return container;
        }
        synchronized (containerCache) {
            container = containerCache.get(jarFileName);
            if (container == null) {
                try {
                    container = createContainer(jarFilePath, packagesToScan, isSpring);
                } catch (Exception e) {
                    LoggerHelper.error("create container for " + jarFileName + " failed.", e);
                    throw new NiubiException(e);
                }
                containerCache.put(jarFileName, container);
            }
            return container;
        }
    }

    public Container createContainer(String jarFilePath, String packagesToScan, boolean isSpring) throws Exception {
        String containerClassName;
        if (isSpring) {
            containerClassName = "com.zuoxiaolong.niubi.job.spring.container.DefaultSpringContainer";
        } else {
            containerClassName = "com.zuoxiaolong.niubi.job.scheduler.container.DefaultContainer";
        }
        ClassLoader jarApplicationClassLoader = ApplicationClassLoaderFactory.getJarApplicationClassLoader(jarFilePath);
        Class<? extends Container> containerClass = (Class<? extends Container>) jarApplicationClassLoader.loadClass(containerClassName);
        Class<?>[] parameterTypes = new Class[]{ClassLoader.class, Properties.class, String.class, String.class};
        Constructor<? extends Container> containerConstructor = containerClass.getConstructor(parameterTypes);
        Properties properties = Bootstrap.properties();
        properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, JarFileHelper.getJarFileName(jarFilePath));
        return containerConstructor.newInstance(jarApplicationClassLoader, properties, packagesToScan, jarFilePath);
    }

}
