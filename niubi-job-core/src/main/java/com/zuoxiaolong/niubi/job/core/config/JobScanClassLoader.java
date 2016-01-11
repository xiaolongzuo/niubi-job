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

package com.zuoxiaolong.niubi.job.core.config;

import com.zuoxiaolong.niubi.job.core.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Xiaolong Zuo
 * @since 16/1/12 03:39
 */
public class JobScanClassLoader extends URLClassLoader {

    private String classpath;

    public JobScanClassLoader(URL[] urls, ClassLoader parent, String classpath) {
        super(urls, parent);
        this.classpath = classpath;
    }

    public void addJobJar(String jarFileName) {
        try {
            String url = classpath;
            if (!classpath.endsWith("/")) {
                url += "/";
            }
            super.addURL(new URL(url + jarFileName));
        } catch (Exception e) {
            LoggerHelper.error("can't find jar.", e);
            throw new NiubiException(e);
        }
    }

}
