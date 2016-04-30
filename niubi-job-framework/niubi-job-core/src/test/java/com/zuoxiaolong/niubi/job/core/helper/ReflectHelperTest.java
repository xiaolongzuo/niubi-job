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

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class ReflectHelperTest {

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Parent {

        private String a;

        private Integer b;

        private Date c;

    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = false)
    public static class Child extends Parent {

        private String d;

        private Integer e;

        private Date f;

    }

    @Test
    public void copyFieldValues() {
        Parent parent = new Parent();
        parent.setA("a");
        parent.setB(1);
        Parent parentCopy = new Parent();
        parentCopy.setC(new Date(1000000L));
        ReflectHelper.copyFieldValues(parent, parentCopy);
        Assert.assertEquals(parentCopy.getA(), "a");
        Assert.assertTrue(parentCopy.getB() == 1);
        Assert.assertNull(parentCopy.getC());
    }

    @Test
    public void copyFieldValuesSkipNull() {
        Parent parent = new Parent();
        parent.setA("a");
        parent.setB(1);
        Parent parentCopy = new Parent();
        parentCopy.setC(new Date(1000000L));
        ReflectHelper.copyFieldValuesSkipNull(parent, parentCopy);
        Assert.assertEquals(parentCopy.getA(), "a");
        Assert.assertTrue(parentCopy.getB() == 1);
        Assert.assertTrue(parentCopy.getC().getTime() == 1000000L);
    }

    @Test
    public void getAllFields() {
        Field[] fields = ReflectHelper.getAllFields(Child.class);
        Assert.assertNotNull(fields);
        Assert.assertTrue(fields.length == 6);
        Assert.assertTrue(fields[0].getName().equals("d"));
        Assert.assertTrue(fields[1].getName().equals("e"));
        Assert.assertTrue(fields[2].getName().equals("f"));
        Assert.assertTrue(fields[3].getName().equals("a"));
        Assert.assertTrue(fields[4].getName().equals("b"));
        Assert.assertTrue(fields[5].getName().equals("c"));
    }

    @Test
    public void getGetterMethod() throws NoSuchFieldException {
        Assert.assertNotNull(ReflectHelper.getGetterMethod(Child.class, "a"));
    }

    @Test
    public void getSetterMethod() throws NoSuchFieldException {
        Assert.assertNotNull(ReflectHelper.getSetterMethod(Child.class, Parent.class.getDeclaredField("a")));
    }

    @Test
    public void getInheritMethod() throws NoSuchMethodException {
        Assert.assertNotNull(ReflectHelper.getInheritMethod(Child.class, "getC", new Class[0]));
    }

    @Test
    public void getFieldValueWithGetterMethod() {
        Parent parent = new Parent();
        parent.setA("1");
        Object a = ReflectHelper.getFieldValueWithGetterMethod(parent, parent.getClass(), "a");
        Assert.assertEquals(a, "1");
    }

    @Test
    public void setFieldValueWithSetterMethod() throws NoSuchFieldException {
        Parent parent = new Parent();
        ReflectHelper.setFieldValueWithSetterMethod(parent, "1", parent.getClass(), parent.getClass().getDeclaredField("a"));
        Assert.assertEquals(parent.getA(), "1");
    }

}
