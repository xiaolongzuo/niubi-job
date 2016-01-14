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

package com.zuoxiaolong.niubi.job.core.io;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Xiaolong Zuo
 * @since 16/1/14 22:44
 */
public class FileSystemResource implements Resource {

    private InputStream inputStream;

    public FileSystemResource(String fileName) {
        try {
            this.inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new NiubiException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

}
