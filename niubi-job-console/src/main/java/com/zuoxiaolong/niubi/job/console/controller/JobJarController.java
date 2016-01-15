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


package com.zuoxiaolong.niubi.job.console.controller;

import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.IOHelper;
import com.zuoxiaolong.niubi.job.service.JobJarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:23
 */
@Controller
@RequestMapping("/jobJar")
public class JobJarController {

    @Autowired
    private JobJarService jobJarService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(String packagesToScan, @RequestParam MultipartFile jobJar, HttpServletRequest request) {
        String jarFilePath = request.getServletContext().getRealPath("job/" + jobJar.getOriginalFilename());
        try {
            IOHelper.writeFile(jarFilePath, jobJar.getBytes());
            jobJarService.save(jarFilePath, packagesToScan);
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        return "job_list";
    }

}
