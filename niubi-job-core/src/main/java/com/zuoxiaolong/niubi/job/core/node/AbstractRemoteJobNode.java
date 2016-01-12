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

package com.zuoxiaolong.niubi.job.core.node;

import com.zuoxiaolong.niubi.job.core.container.Container;
import com.zuoxiaolong.niubi.job.core.container.DefaultContainer;

import java.util.Collections;
import java.util.Map;
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
        this.containerCache = new ConcurrentHashMap<>();
    }

    protected Map<String, Container> getContainerCache() {
        return Collections.unmodifiableMap(containerCache);
    }

    public Container getContainer(String jarUrl) {
        Container container = containerCache.get(jarUrl);
        if (container != null) {
            return container;
        }
        lock.lock();
        try {
            container = containerCache.get(jarUrl);
            if (container == null) {
                container = new DefaultContainer(jarUrl);
                containerCache.put(jarUrl, container);
            }
            return container;
        } finally {
            lock.unlock();
        }
    }

}
