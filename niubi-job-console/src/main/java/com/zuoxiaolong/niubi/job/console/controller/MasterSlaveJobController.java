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

import com.zuoxiaolong.niubi.job.console.exception.ExceptionForward;
import com.zuoxiaolong.niubi.job.core.exception.NiubiException;
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.core.helper.IOHelper;
import com.zuoxiaolong.niubi.job.service.MasterSlaveJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Controller
@RequestMapping("/masterSlaveJobs")
public class MasterSlaveJobController extends AbstractController {

    @Autowired
    private MasterSlaveJobService masterSlaveJobService;

    @RequestMapping(value = "")
    public String list(Model model) {
        model.addAttribute("jobs", masterSlaveJobService.getAllJobs());
        return "master_slave_job_list";
    }

    @RequestMapping(value = "/upload")
    public String upload() {
        return "master_slave_job_upload";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ExceptionForward("/masterSlaveJobSummaries")
    public String upload(String packagesToScan, @RequestParam MultipartFile jobJar) {
        AssertHelper.notNull(jobJar, "jobJar can't be null.");
        AssertHelper.notEmpty(packagesToScan, "packagesToScan can't be empty.");
        String jarFilePath = getDirectoryRealPath("job/masterSlave") + "/" + jobJar.getOriginalFilename();
        try {
            IOHelper.writeFile(jarFilePath, jobJar.getBytes());
            masterSlaveJobService.saveJob(jarFilePath, packagesToScan);
        } catch (IOException e) {
            throw new NiubiException(e);
        }
        return "forward:/masterSlaveJobSummaries";
    }

}
