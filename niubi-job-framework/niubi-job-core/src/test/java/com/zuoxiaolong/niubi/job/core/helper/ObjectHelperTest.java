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

import java.beans.Transient;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class ObjectHelperTest {

    @Test
    public void isEmpty() {
        Assert.assertTrue(ObjectHelper.isEmpty(null));
        Assert.assertTrue(ObjectHelper.isEmpty(""));
        Assert.assertFalse(ObjectHelper.isEmpty(new Object()));
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Bean {

        private String a;

        private Integer b;

        private Date c;

        private String dId;

        private String e;

        @Transient
        public String getDId() {
            return dId;
        }

        public String getE() {
            return e;
        }
    }

    @Test
    public void isTransientId() throws NoSuchFieldException {
        Assert.assertFalse(ObjectHelper.isTransientId(Bean.class, Bean.class.getDeclaredField("a")));
        Assert.assertTrue(ObjectHelper.isTransientId(Bean.class, Bean.class.getDeclaredField("dId")));
        Assert.assertFalse(ObjectHelper.isTransientId(Bean.class, Bean.class.getDeclaredField("e")));
    }

}
