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

package com.zuoxiaolong.niubi.job.api.curator;

import com.zuoxiaolong.niubi.job.api.StandbyApiFactory;
import com.zuoxiaolong.niubi.job.api.StandbyJobApi;
import com.zuoxiaolong.niubi.job.api.data.StandbyJobData;
import com.zuoxiaolong.niubi.job.api.helper.PathHelper;
import com.zuoxiaolong.niubi.job.test.zookeeper.ZookeeperClientFactory;
import com.zuoxiaolong.niubi.job.test.zookeeper.ZookeeperServerCluster;
import org.apache.curator.framework.CuratorFramework;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class StandbyJobApiTest {

    private static StandbyApiFactory standbyApiFactory;

    private static StandbyJobApi standbyJobApi;

    private static CuratorFramework client;

    @Before
    public void setup() {
        ZookeeperServerCluster.startZookeeperCluster();
        client = ZookeeperClientFactory.getClient();
        standbyApiFactory = new StandbyApiFactoryImpl(client);
        standbyJobApi = standbyApiFactory.jobApi();
    }

    @After
    public void teardown() {
        client.close();
        ZookeeperServerCluster.stopZookeeperCluster();
    }

    @Test
    public void getAllJobsSaveJob() {
        List<StandbyJobData> list = standbyJobApi.getAllJobs();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() == 0);
        StandbyJobData.Data data = new StandbyJobData.Data();
        data.setMode("Common");
        data.setCron(".....");
        data.setOperation("start");
        standbyJobApi.saveJob("com.com.com", "name1", data);
        standbyJobApi.saveJob("com.com.com", "name2", data);
        standbyJobApi.saveJob("com.com.com","name3",data);
        list = standbyJobApi.getAllJobs();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() == 3);
        for (StandbyJobData jobData : list) {
            Assert.assertTrue(jobData.getData().getCron().equals("....."));
            Assert.assertTrue(jobData.getData().getMode().equals("Common"));
            Assert.assertTrue(jobData.getData().getOperation().equals("start"));
        }
    }

    @Test
    public void updateNodeGetJob() {
        StandbyJobData.Data data = new StandbyJobData.Data();
        data.setMode("Common");
        data.setCron(".....");
        data.setOperation("start");
        standbyJobApi.saveJob("com.com.com", "name1", data);
        StandbyJobData jobData = standbyJobApi.getJob("com.com.com", "name1");
        Assert.assertNotNull(jobData);
        Assert.assertEquals("Common", jobData.getData().getMode());
        Assert.assertNull(jobData.getData().getJarFileName());
        jobData = standbyJobApi.getJob(PathHelper.getJobPath(standbyApiFactory.pathApi().getJobPath(), "com.com.com", "name1"));
        Assert.assertNotNull(jobData);
        Assert.assertEquals("Common", jobData.getData().getMode());
        Assert.assertNull(jobData.getData().getJarFileName());
        data.setJarFileName("1.jar");
        standbyJobApi.updateJob("com.com.com", "name1", data);
        jobData = standbyJobApi.getJob("com.com.com", "name1");
        Assert.assertNotNull(jobData);
        Assert.assertNotNull(jobData);
        Assert.assertEquals("Common", jobData.getData().getMode());
        Assert.assertEquals("1.jar", jobData.getData().getJarFileName());
    }

}
