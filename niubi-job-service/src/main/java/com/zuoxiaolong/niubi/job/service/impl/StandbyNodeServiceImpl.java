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

import com.zuoxiaolong.niubi.job.api.data.StandbyNodeData;
import com.zuoxiaolong.niubi.job.core.helper.ReflectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.StandbyNode;
import com.zuoxiaolong.niubi.job.service.StandbyNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:04
 */
@Service
public class StandbyNodeServiceImpl extends AbstractService implements StandbyNodeService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public List<StandbyNode> getAllNodes() {
        return baseDao.getAll(StandbyNode.class);
    }

    @Override
    public void saveNode(StandbyNodeData standbyNodeData) {
        StandbyNode param = new StandbyNode();
        param.setPath(standbyNodeData.getPath());
        StandbyNode standbyNodeInDb = baseDao.getUnique(StandbyNode.class, param);
        boolean exists = true;
        if (standbyNodeInDb == null) {
            standbyNodeInDb = new StandbyNode();
            standbyNodeInDb.setPath(standbyNodeData.getPath());
            exists = false;
        }
        ReflectHelper.copyFieldValues(standbyNodeData.getData(), standbyNodeInDb);
        if (exists) {
            baseDao.update(standbyNodeInDb);
        } else {
            baseDao.save(standbyNodeInDb);
        }
    }

}
