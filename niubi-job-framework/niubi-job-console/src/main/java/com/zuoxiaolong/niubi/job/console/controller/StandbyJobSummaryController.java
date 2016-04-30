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
import com.zuoxiaolong.niubi.job.core.helper.AssertHelper;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyJobSummary;
import com.zuoxiaolong.niubi.job.service.StandbyJobService;
import com.zuoxiaolong.niubi.job.service.StandbyJobSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Controller
@RequestMapping("/standbyJobSummaries")
public class StandbyJobSummaryController extends AbstractController {

    @Autowired
    private StandbyJobSummaryService standbyJobSummaryService;

    @Autowired
    private StandbyJobService standbyJobService;

    @RequestMapping(value = "")
    public String list(Model model) {
        model.addAttribute("jobSummaries", standbyJobSummaryService.getAllJobSummaries());
        return "standby_job_summary_list";
    }

    @RequestMapping(value = "/{id}")
    public String input(@PathVariable String id, Model model) {
        StandbyJobSummary standbyJobSummary = standbyJobSummaryService.getJobSummary(id);
        model.addAttribute("jobSummary", standbyJobSummary);
        model.addAttribute("jarFileNameList", standbyJobService.getJarFileNameList(standbyJobSummary.getGroupName(), standbyJobSummary.getJobName()));
        return "standby_job_summary_input";
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ExceptionForward("/standbyJobSummaries")
    public String update(StandbyJobSummary standbyJobSummary) {
        AssertHelper.notEmpty(standbyJobSummary.getJobCron(), "cron can't be empty.");
        standbyJobSummaryService.saveJobSummary(standbyJobSummary);
        return success("/standbyJobSummaries");
    }

    @RequestMapping(value = "/{id}/synchronize")
    @ExceptionForward("/standbyJobSummaries")
    public String synchronize(@PathVariable String id) {
        AssertHelper.notEmpty(id, "id can't be empty.");
        standbyJobSummaryService.updateJobSummary(id);
        return success("/standbyJobSummaries");
    }

}
