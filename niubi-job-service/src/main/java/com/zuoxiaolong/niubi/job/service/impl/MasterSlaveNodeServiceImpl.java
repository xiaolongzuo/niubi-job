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

import com.zuoxiaolong.niubi.job.api.data.MasterSlaveNodeData;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveJobSummary;
import com.zuoxiaolong.niubi.job.persistent.entity.MasterSlaveNode;
import com.zuoxiaolong.niubi.job.service.MasterSlaveNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class MasterSlaveNodeServiceImpl extends AbstractService implements MasterSlaveNodeService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public List<MasterSlaveNode> getAllNodes() {
        List<MasterSlaveNodeData> masterSlaveNodeDataList;
        List<MasterSlaveNode> masterNodeViewList = new ArrayList<>();
        try {
            masterSlaveNodeDataList = masterSlaveApiFactory.nodeApi().getAllNodes();
        } catch (Exception e) {
            LoggerHelper.warn("select all standby nodes failed, has been ignored [" + e.getClass().getName() + ", " + e.getMessage() + "]");
            return masterNodeViewList;
        }
        for (MasterSlaveNodeData masterSlaveNodeData : masterSlaveNodeDataList) {
            MasterSlaveNode masterNodeView = new MasterSlaveNode();
            masterNodeView.setPath(masterSlaveNodeData.getPath());
            if (masterSlaveNodeData.getData() != null) {
                ReflectHelper.copyFieldValues(masterSlaveNodeData.getData(), masterNodeView);
            }
            masterNodeViewList.add(masterNodeView);
        }
        return masterNodeViewList;
    }

    @Override
    public void saveNode(MasterSlaveNodeData masterSlaveNodeData) {
        MasterSlaveNode param = new MasterSlaveNode();
        param.setPath(masterSlaveNodeData.getPath());
        MasterSlaveNode masterSlaveNodeInDb = baseDao.getUnique(MasterSlaveNode.class, param);
        boolean exists = true;
        if (masterSlaveNodeInDb == null) {
            masterSlaveNodeInDb = new MasterSlaveNode();
            masterSlaveNodeInDb.setPath(masterSlaveNodeData.getPath());
            exists = false;
        }
        ReflectHelper.copyFieldValues(masterSlaveNodeData.getData(), masterSlaveNodeInDb);
        List<String> jobPaths = masterSlaveNodeData.getData().getJobPaths();
        List<MasterSlaveJobSummary> jobSummaries = new ArrayList<>();
        for (String jobPath : jobPaths) {
            MasterSlaveJobSummary jobSummaryParam = new MasterSlaveJobSummary();
            jobSummaryParam.setPath(jobPath);
            jobSummaries.add(baseDao.getUnique(MasterSlaveJobSummary.class, jobSummaryParam));
        }
        masterSlaveNodeInDb.setJobSummaries(jobSummaries);
        if (exists) {
            baseDao.update(masterSlaveNodeInDb);
        } else {
            baseDao.save(masterSlaveNodeInDb);
        }
    }

}
