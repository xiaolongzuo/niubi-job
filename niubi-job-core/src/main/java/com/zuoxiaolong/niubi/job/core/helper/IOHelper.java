package com.zuoxiaolong.niubi.job.core.helper;

/*
 * Copyright 2002-2015 the original author or authors.
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * IO帮助类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface IOHelper {

    /**
     * 将数据写入文件
     *
     * @param fileName 文件名称
     * @param bytes 数据
     * @throws IOException
     */
    static void writeFile(String fileName, byte[] bytes) throws IOException {
        if (fileName != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }

    static void writeStream(String content, String charset, OutputStream outputStream) throws IOException {
        if (content != null) {
            outputStream.write(content.getBytes(charset));
            outputStream.flush();
        }
    }

    static String readStream(HttpURLConnection connection, String charset) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return IOHelper.readStream(connection.getInputStream(), charset);
        } else {
            return IOHelper.readStream(connection.getErrorStream(), charset);
        }
    }

    static String readStream(InputStream inputStream, String charset) throws IOException {
        byte[] result = readStreamBytes(inputStream);
        if (result == null ) {
            return null;
        }
        return new String(result, charset);
    }

    static byte[] readStreamBytes(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return IOHelper.readStreamBytes(connection.getInputStream());
        } else {
            return IOHelper.readStreamBytes(connection.getErrorStream());
        }
    }

    static byte[] readStreamBytes(InputStream inputStream) throws IOException {
        byte[] cache = new byte[2048];
        int len;
        byte[] bytes = new byte[0];
        while ((len = inputStream.read(cache)) > 0) {
            byte[] temp = bytes;
            bytes = new byte[bytes.length + len];
            System.arraycopy(temp, 0, bytes, 0, temp.length);
            System.arraycopy(cache, 0, bytes, temp.length, len);
        }
        if (bytes.length == 0) {
            return null;
        }
        return bytes;
    }

    static byte[] readStreamBytesAndClose(InputStream inputStream) throws IOException {
        byte[] bytes = readStreamBytes(inputStream);
        inputStream.close();
        return bytes;
    }

}
