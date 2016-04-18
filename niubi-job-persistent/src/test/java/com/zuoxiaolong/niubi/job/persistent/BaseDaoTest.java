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

package com.zuoxiaolong.niubi.job.persistent;

import com.zuoxiaolong.niubi.job.persistent.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.4.2
 */
public class BaseDaoTest extends AbstractSpringTestCase {

    @Autowired
    private BaseDao baseDao;

    @Test
    public void baseAutowired() {
        Assert.assertNotNull(baseDao);
    }

    @Test
    public void save() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        User userInDb = baseDao.get(User.class, id);
        Assert.assertNotNull(userInDb);
        Assert.assertEquals(userInDb.getUserName(), "123");
        Assert.assertEquals(userInDb.getUserPassword(), "234");
        Assert.assertEquals(userInDb.getPasswordSalt(), "123");
        baseDao.delete(userInDb);
    }

    @Test
    public void update() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        User userInDb = baseDao.get(User.class, id);
        userInDb.setUserName("aaa");
        userInDb.setUserPassword("bbb");
        userInDb.setPasswordSalt("ccc");
        baseDao.update(userInDb);
        userInDb = baseDao.get(User.class, id);
        Assert.assertNotNull(userInDb);
        Assert.assertEquals(userInDb.getUserName(), "aaa");
        Assert.assertEquals(userInDb.getUserPassword(), "bbb");
        Assert.assertEquals(userInDb.getPasswordSalt(), "ccc");
        baseDao.delete(userInDb);
    }

    @Test
    public void delete() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        User userInDb = baseDao.get(User.class, id);
        Assert.assertNotNull(userInDb);
        baseDao.delete(userInDb);
        userInDb = baseDao.get(User.class, id);
        Assert.assertNull(userInDb);
    }

    @Test
    public void getAll() {
        User user1 = new User();
        user1.setUserName("1");
        user1.setUserPassword("1");
        user1.setPasswordSalt("1");
        User user2 = new User();
        user2.setUserName("2");
        user2.setUserPassword("2");
        user2.setPasswordSalt("2");
        User user3 = new User();
        user3.setUserName("3");
        user3.setUserPassword("3");
        user3.setPasswordSalt("3");
        String id1 = baseDao.save(user1);
        String id2 = baseDao.save(user2);
        String id3 = baseDao.save(user3);
        List<User> users = baseDao.getAll(User.class);
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 3);
        Assert.assertNotNull(users.get(0));
        Assert.assertEquals(users.get(0).getUserName(), "1");
        Assert.assertEquals(users.get(0).getUserPassword(), "1");
        Assert.assertEquals(users.get(0).getPasswordSalt(), "1");
        Assert.assertEquals(users.get(0).getId(), id1);
        Assert.assertNotNull(users.get(0));
        Assert.assertEquals(users.get(1).getUserName(), "2");
        Assert.assertEquals(users.get(1).getUserPassword(), "2");
        Assert.assertEquals(users.get(1).getPasswordSalt(), "2");
        Assert.assertEquals(users.get(1).getId(), id2);
        Assert.assertNotNull(users.get(2));
        Assert.assertEquals(users.get(2).getUserName(), "3");
        Assert.assertEquals(users.get(2).getUserPassword(), "3");
        Assert.assertEquals(users.get(2).getPasswordSalt(), "3");
        Assert.assertEquals(users.get(2).getId(), id3);
        baseDao.delete(user1);
        baseDao.delete(user2);
        baseDao.delete(user3);
    }

    @Test
    public void get() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        User userInDb = baseDao.get(User.class, id);
        Assert.assertNotNull(userInDb);
        Assert.assertEquals(userInDb.getUserName(), "123");
        Assert.assertEquals(userInDb.getUserPassword(), "234");
        Assert.assertEquals(userInDb.getPasswordSalt(), "123");
        baseDao.delete(userInDb);
    }

    @Test
    public void getList() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        List<User> users = baseDao.getList(User.class, user);
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 1);
        Assert.assertNotNull(users.get(0));
        Assert.assertEquals(users.get(0).getUserName(), "123");
        Assert.assertEquals(users.get(0).getUserPassword(), "234");
        Assert.assertEquals(users.get(0).getPasswordSalt(), "123");
        Assert.assertEquals(users.get(0).getId(), id);
        baseDao.delete(users.get(0));
    }

    @Test
    public void getUnique() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        User userInDb = baseDao.getUnique(User.class, user);
        Assert.assertNotNull(userInDb);
        Assert.assertEquals(userInDb.getUserName(), "123");
        Assert.assertEquals(userInDb.getUserPassword(), "234");
        Assert.assertEquals(userInDb.getPasswordSalt(), "123");
        Assert.assertEquals(userInDb.getId(), id);
        baseDao.delete(userInDb);
    }

    @Test
    public void getByPager() {
        User user = new User();
        user.setUserName("123");
        user.setUserPassword("234");
        user.setPasswordSalt("123");
        String id = baseDao.save(user);
        Pager<User> pager = new Pager<>();
        Pager<User> pagerInDb = baseDao.getByPager(User.class, pager, user, false);
        Assert.assertNotNull(pagerInDb);
        Assert.assertTrue(pagerInDb.getTotalCount() == 1);
        Assert.assertTrue(pagerInDb.getViewJsonData().getRows().size() == 1);
        Assert.assertEquals(pagerInDb.getViewJsonData().getRows().get(0).getUserName(), "123");
        Assert.assertEquals(pagerInDb.getViewJsonData().getRows().get(0).getUserPassword(), "234");
        Assert.assertEquals(pagerInDb.getViewJsonData().getRows().get(0).getPasswordSalt(), "123");
        Assert.assertEquals(pagerInDb.getViewJsonData().getRows().get(0).getId(), id);
        baseDao.delete(pagerInDb.getViewJsonData().getRows().get(0));
    }

}
