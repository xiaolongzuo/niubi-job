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

package com.zuoxiaolong.niubi.job.core.helper;

import java.io.File;
import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface JarFileHelper {

    static String getJarFileName(String jarFilePath) {
        if (jarFilePath == null) {
            return null;
        }
        if (jarFilePath.indexOf("/") < 0 && jarFilePath.indexOf("\\") < 0) {
            return jarFilePath;
        }
        String jarFileName = jarFilePath.substring(jarFilePath.lastIndexOf("/") + 1);
        return jarFileName.substring(jarFileName.lastIndexOf("\\") + 1);
    }

    static String downloadJarFile(String jarFileParentPath, String jarUrl) throws IOException {
        String jarFileName = jarUrl.substring(jarUrl.lastIndexOf("/") + 1);
        String jarFilePath = StringHelper.appendSlant(jarFileParentPath) + jarFileName;
        File file = new File(jarFilePath);
        if (file.exists()) {
            return jarFilePath;
        }
        return HttpHelper.downloadRemoteResource(jarFilePath, jarUrl);
    }

}
