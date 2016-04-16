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

    /**
     * 读取输入流中的字节
     *
     * @param inputStream 输入流
     * @return 读出来的字节数组
     * @throws IOException
     */
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
