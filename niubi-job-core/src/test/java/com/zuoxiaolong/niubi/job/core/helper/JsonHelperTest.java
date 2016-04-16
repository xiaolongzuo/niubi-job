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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class JsonHelperTest {

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Bean {

        private String a;

        private Integer b;

        private Date c;

    }

    @Test
    public void toBytes() throws UnsupportedEncodingException {
        Bean bean = new Bean();
        bean.setA("aaaa");
        bean.setB(123);
        bean.setC(new Date(1000000L));
        byte[] bytes = JsonHelper.toBytes(bean);
        byte[] originBytes = "{\"a\":\"aaaa\",\"b\":123,\"c\":\"Jan 1, 1970 8:16:40 AM\"}".getBytes("UTF-8");
        Assert.assertEquals(bytes.length, originBytes.length);
        for (int i = 0; i < bytes.length;i++) {
            Assert.assertEquals(bytes[i], originBytes[i]);
        }
    }

    @Test
    public void toJson() {
        Bean bean = new Bean();
        bean.setA("aaaa");
        bean.setB(123);
        bean.setC(new Date(1000000L));
        String json = JsonHelper.toJson(bean);
        Assert.assertEquals("{\"a\":\"aaaa\",\"b\":123,\"c\":\"Jan 1, 1970 8:16:40 AM\"}", json);
    }

    @Test
    public void fromJson() {
        Bean originBean = new Bean();
        originBean.setA("aaaa");
        originBean.setB(123);
        originBean.setC(new Date(1000000L));
        Bean bean = JsonHelper.fromJson("{\"a\":\"aaaa\",\"b\":123,\"c\":\"Jan 1, 1970 8:16:40 AM\"}", Bean.class);
        Assert.assertEquals(bean, originBean);
    }

}
