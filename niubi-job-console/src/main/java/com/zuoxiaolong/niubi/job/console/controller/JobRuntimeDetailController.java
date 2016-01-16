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

import com.zuoxiaolong.niubi.job.persistent.entity.JobRuntimeDetail;
import com.zuoxiaolong.niubi.job.service.JobDetailService;
import com.zuoxiaolong.niubi.job.service.JobRuntimeDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:23
 */
@Controller
@RequestMapping("/jobRuntimeDetails")
public class JobRuntimeDetailController {

    @Autowired
    private JobRuntimeDetailService jobRuntimeDetailService;

    @Autowired
    private JobDetailService jobDetailService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("jobRuntimeDetails", jobRuntimeDetailService.getAllStandbyJobRuntimeDetails());
        return "job_runtime_detail_list";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String input(@PathVariable String id, Model model) {
        JobRuntimeDetail jobRuntimeDetail = jobRuntimeDetailService.getStandbyJobRuntimeDetail(id);
        model.addAttribute("jobRuntimeDetail", jobRuntimeDetail);
        model.addAttribute("jarFileNameList", jobDetailService.getJarFileNameList(jobRuntimeDetail.getGroupName(), jobRuntimeDetail.getJobName()));
        return "job_runtime_detail_input";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(JobRuntimeDetail jobRuntimeDetail) {
        jobRuntimeDetailService.createStandbyJob(jobRuntimeDetail);
        return "redirect:/jobRuntimeDetails";
    }

}
