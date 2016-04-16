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

package com.zuoxiaolong.niubi.job.test.http;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
@RestController
@EnableAutoConfiguration
public class DownloadFileController {

    @RequestMapping("/download/test.txt")
    public void downloadTxt(HttpServletResponse response) throws IOException {
        String fileName = "test.txt";
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        OutputStream outputStream = response.getOutputStream();
        outputStream.write("hello".getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @RequestMapping("/download/test.jar")
    public void downloadJar(HttpServletResponse response) throws IOException {
        String fileName = "test.jar";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        OutputStream outputStream = response.getOutputStream();
        outputStream.write("hello".getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
