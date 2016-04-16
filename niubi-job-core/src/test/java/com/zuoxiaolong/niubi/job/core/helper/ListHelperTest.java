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

package com.zuoxiaolong.niubi.job.core.helper;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class ListHelperTest {

    @Test
    public void add() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        List<String> stringList1 = ListHelper.add(list, "3");
        List<String> stringList2 = ListHelper.add(null, "3");
        Assert.assertTrue(stringList1.size() == 3);
        Assert.assertTrue(stringList2.size() == 1);
        Assert.assertTrue(stringList1.get(0).equals("1"));
        Assert.assertTrue(stringList1.get(1).equals("2"));
        Assert.assertTrue(stringList1.get(2).equals("3"));
        Assert.assertTrue(stringList2.get(0).equals("3"));
    }

    @Test
    public void isEmpty1() {
        Assert.assertTrue(ListHelper.isEmpty((Collection)null));
        Assert.assertTrue(ListHelper.isEmpty(new ArrayList<>()));
        Assert.assertFalse(ListHelper.isEmpty(Arrays.asList("1")));
    }

    @Test
    public void isEmpty2() {
        Assert.assertTrue(ListHelper.isEmpty((Object[])null));
        Assert.assertTrue(ListHelper.isEmpty(new Object[0]));
        Assert.assertFalse(ListHelper.isEmpty(new Object[]{"1"}));
    }

}
