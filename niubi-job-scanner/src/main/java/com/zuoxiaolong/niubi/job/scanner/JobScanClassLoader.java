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

import com.zuoxiaolong.niubi.job.core.helper.ListHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 03:39
 */
public class JobScanClassLoader extends URLClassLoader {

    private String[] jarFilePaths;

    JobScanClassLoader(ClassLoader parent) {
        super(new URL[]{}, parent);
        this.jarFilePaths = StringHelper.emptyArray();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    public String[] getJarFilePaths() {
        return jarFilePaths;
    }

    public synchronized void addJarFiles(String... jarFilePaths) {
        if (ListHelper.isEmpty(jarFilePaths)) {
            return;
        }
        this.jarFilePaths = StringHelper.mergeArray(this.jarFilePaths, jarFilePaths);
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
