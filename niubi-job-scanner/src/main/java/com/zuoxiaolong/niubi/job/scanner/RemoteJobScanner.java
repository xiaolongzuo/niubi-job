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

import com.zuoxiaolong.niubi.job.core.helper.ClassHelper;
import com.zuoxiaolong.niubi.job.core.helper.IOHelper;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.StringHelper;
import com.zuoxiaolong.niubi.job.scanner.job.JobDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 默认的任务扫描器
 *
 * @author Xiaolong Zuo
 * @since 16/1/9 00:45
 */
public class RemoteJobScanner extends AbstractJobScanner {

    private String[] jarFilePaths;

    private List<String> packagesToScan = Collections.emptyList();

    public RemoteJobScanner(ClassLoader classLoader, String jarFilePath, String packagesToScan) {
        super(new JobScanClassLoader(classLoader));
        this.jarFilePaths = new String[]{jarFilePath};
        this.packagesToScan = StringHelper.splitToList(packagesToScan);
    }

    public RemoteJobScanner(JobScanClassLoader classLoader, String... jarUrls) {
        super(classLoader);
        if (jarUrls != null) {
            this.jarFilePaths = new String[jarUrls.length];
            for (int i = 0;i < this.jarFilePaths.length; i++) {
                try {
                    this.jarFilePaths[i] = downloadJarFile(jarUrls[i]);
                } catch (IOException e) {
                    LoggerHelper.error("download jar file [" + jarUrls[i] + "] failed,has been ignored.");
                }
            }
        }
    }

    private String downloadJarFile(String jarUrl) throws IOException {
        String jarFileName = jarUrl.substring(jarUrl.lastIndexOf("/") + 1);
        String jarFilePath = classLoader.getResource("").getFile() + jarFileName;
        File file = new File(jarFilePath);
        if (file.exists()) {
            return jarFilePath;
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(jarUrl).openConnection();
        connection.connect();
        byte[] bytes = IOHelper.readStreamBytesAndClose(connection.getInputStream());
        IOHelper.writeFile(jarFilePath, bytes);
        return jarFilePath;
    }

    @Override
    public List<JobDescriptor> scan() {
        List<JobDescriptor> descriptorList = new ArrayList<>();
        try {
            for (String jarFilePath : jarFilePaths) {
                File file = new File(jarFilePath);
                if (file.exists()) {
                    classLoader.addURL(file.toURI().toURL());
                    scan(descriptorList, jarFilePath);
                } else {
                    LoggerHelper.warn("jar file [" + jarFilePath + "] can't be found.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return descriptorList;
    }

    private void scan(List<JobDescriptor> descriptorList, String jarFilePath) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
        while (jarEntryEnumeration.hasMoreElements()) {
            String jarEntryName = jarEntryEnumeration.nextElement().getName();
            if (jarEntryName == null || !jarEntryName.endsWith(".class")) {
                continue;
            }
            String packageName = ClassHelper.getPackageName(jarEntryName);
            if (packagesToScan.size() > 0 && !packagesToScan.contains(packageName)) {
                continue;
            }
            String className = ClassHelper.getClassName(jarEntryName);
            super.scanClass(className, descriptorList);
        }
    }

}
