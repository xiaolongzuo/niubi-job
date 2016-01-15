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


package com.zuoxiaolong.niubi.job.service.impl;

import com.zuoxiaolong.niubi.job.api.data.JobData;
import com.zuoxiaolong.niubi.job.persistent.entity.Job;
import com.zuoxiaolong.niubi.job.service.JobService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobServiceImpl extends AbstractService implements JobService {

    @Override
    public List<Job> selectAllStandbyJobs() {
        List<JobData> jobModelList = apiFactory.jobApi().selectAllStandbyJobs();
        List<Job> jobList = new ArrayList<>();
        for (JobData jobData : jobModelList) {
            Job job = new Job();
            job.setGroup(jobData.getId().substring(0, jobData.getId().lastIndexOf(".")));
            job.setName(jobData.getId().substring(jobData.getId().lastIndexOf(".") + 1));
            if (jobData.getData() != null) {
                job.setJarFileName(jobData.getData().getJarFileName());
                job.setMode(jobData.getData().getMode().name());
                job.setState(jobData.getData().getState().name());
                job.setCron(jobData.getData().getCron());
                job.setMisfirePolicy(jobData.getData().getMisfirePolicy().name());
            }
            jobList.add(job);
        }
        return jobList;
    }

}
