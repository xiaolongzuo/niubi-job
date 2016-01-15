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

import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.Job;
import com.zuoxiaolong.niubi.job.persistent.entity.JobJar;
import com.zuoxiaolong.niubi.job.service.JobJarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class JobJarServiceImpl extends AbstractService implements JobJarService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public void save(String jarFileName) {
        JobJar jobJar = new JobJar();
        jobJar.setJarFileName(jarFileName);
        List<Job> jobs = new ArrayList<>();

        jobJar.setJobs(jobs);
        baseDao.save(jobJar);
    }

}
