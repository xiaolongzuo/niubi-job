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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class HttpHelper {

    public static String downloadRemoteResource(String filePath, String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        byte[] bytes = IOHelper.readStreamBytesAndClose(connection.getInputStream());
        IOHelper.writeFile(filePath, bytes);
        return filePath;
    }

}
