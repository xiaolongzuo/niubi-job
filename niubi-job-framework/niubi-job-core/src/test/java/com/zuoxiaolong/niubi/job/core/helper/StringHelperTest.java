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


package com.zuoxiaolong.niubi.job.core.helper;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class StringHelperTest {

    @Test
    public void appendSlant() {
        Assert.assertNull(StringHelper.appendSlant(null));
        Assert.assertEquals("api/v1/", StringHelper.appendSlant("api/v1/"));
        Assert.assertEquals("api/v1/", StringHelper.appendSlant("api/v1"));
    }

    @Test
    public void getBytes() throws UnsupportedEncodingException {
        Assert.assertEquals(0, StringHelper.getBytes(null).length);
        byte[] bytes = "test".getBytes("UTF-8");
        byte[] testBytes = StringHelper.getBytes("test");
        Assert.assertArrayEquals(bytes, testBytes);
    }

    @Test
    public void mergeArray() throws UnsupportedEncodingException {
        String[] array1 = new String[]{"test1"};
        String[] array2 = new String[]{"test2"};
        Assert.assertArrayEquals(array1, StringHelper.mergeArray(array1, null));
        Assert.assertArrayEquals(array1, StringHelper.mergeArray(null, array1));
        Assert.assertNull(StringHelper.mergeArray(null, null));
        Assert.assertArrayEquals(new String[]{"test1", "test2"}, StringHelper.mergeArray(array1, array2));
    }

    @Test
    public void emptyArray() throws UnsupportedEncodingException {
        Assert.assertEquals(0, StringHelper.emptyArray().length);
    }

    @Test
    public void emptyString() throws UnsupportedEncodingException {
        Assert.assertEquals("", StringHelper.emptyString());
    }

    @Test
    public void isEmpty() throws UnsupportedEncodingException {
        Assert.assertTrue(StringHelper.isEmpty((String)null));
        Assert.assertTrue(StringHelper.isEmpty(StringHelper.emptyString()));
        Assert.assertFalse(StringHelper.isEmpty("1"));

        Assert.assertTrue(StringHelper.isEmpty((String[])null));
        Assert.assertTrue(StringHelper.isEmpty(StringHelper.emptyArray()));
        Assert.assertFalse(StringHelper.isEmpty(new String[]{"1"}));

        Assert.assertEquals("1", StringHelper.isEmpty(null, "1"));
        Assert.assertEquals("1", StringHelper.isEmpty(StringHelper.emptyString(), "1"));
        Assert.assertEquals("2", StringHelper.isEmpty("2", "1"));
    }

    @Test
    public void checkEmpty() throws UnsupportedEncodingException {
        Assert.assertEquals(0, StringHelper.checkEmpty(null).length);
        Assert.assertEquals(0, StringHelper.checkEmpty(new String[0]).length);
        Assert.assertArrayEquals(new String[]{"test1","test2"}, StringHelper.checkEmpty(new String[]{"test1","test2"}));
    }

    @Test
    public void split() throws UnsupportedEncodingException {
        String s1 = "1,2,3,4";
        String s2 = "1;2;3;4";
        String s3 = "1:2:3:4";
        String[] array = new String[]{"1","2","3","4"};
        Assert.assertEquals(0, StringHelper.split(null).length);
        Assert.assertEquals(0, StringHelper.split(StringHelper.emptyString()).length);
        Assert.assertArrayEquals(array, StringHelper.split(s1));
        Assert.assertArrayEquals(array, StringHelper.split(s2));
        Assert.assertArrayEquals(array, StringHelper.split(s3));

        List<String> list = Arrays.asList(array);

        List<String> l1 = StringHelper.splitToList(s1);
        List<String> l2 = StringHelper.splitToList(s2);
        List<String> l3 = StringHelper.splitToList(s3);
        for (int i = 0;i < list.size(); i++) {
            Assert.assertEquals(list.get(i), l1.get(i));
            Assert.assertEquals(list.get(i), l2.get(i));
            Assert.assertEquals(list.get(i), l3.get(i));
        }
    }
}
