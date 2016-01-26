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

package com.zuoxiaolong.niubi.job.service;

import com.zuoxiaolong.niubi.job.api.data.MasterSlaveJobData;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public interface MasterSlaveJobSummaryService {

    List<MasterSlaveJobSummary> getAllJobSummaries();

    void saveJobSummary(MasterSlaveJobSummary standbyJobSummary);

    MasterSlaveJobSummary getJobSummary(String id);

    void updateJobSummary(MasterSlaveJobData.Data data);

    void updateJobSummary(String id);

}
