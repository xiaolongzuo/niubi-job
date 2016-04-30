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

package com.zuoxiaolong.niubi.job.api.helper;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class PathHelperTest {

    @Test
    public void getParentPath() {
        try {
            PathHelper.getParentPath(null);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e.getClass() == IllegalArgumentException.class);
        }
        Assert.assertTrue("".equals(PathHelper.getParentPath("")));
        Assert.assertTrue("/test/1/12/3/4".equals(PathHelper.getParentPath("/test/1/12/3/4/5")));
    }

    @Test
    public void getJobPath() {
        try {
            PathHelper.getJobPath(null, null, null);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e.getClass() == IllegalArgumentException.class);
        }
        Assert.assertEquals("/1/2/3/group.name", PathHelper.getJobPath("/1/2/3", "group", "name"));
    }

    @Test
    public void getEndPath() {
        try {
            PathHelper.getEndPath(null);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e.getClass() == IllegalArgumentException.class);
        }
        Assert.assertEquals("5", PathHelper.getEndPath("/1/2/3/4/5"));
    }

}
