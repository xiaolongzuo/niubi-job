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

import com.zuoxiaolong.niubi.job.core.helper.ObjectHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.User;
import com.zuoxiaolong.niubi.job.persistent.shiro.HashHelper;
import com.zuoxiaolong.niubi.job.service.ServiceException;
import com.zuoxiaolong.niubi.job.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public void updatePassword(String username, String password) {
        User param = new User();
        param.setUserName(username);
        User userInDb = baseDao.getUnique(User.class, param);
        if (ObjectHelper.isEmpty(userInDb)) {
            throw new ServiceException("can't find user.");
        }
        userInDb.setUserPassword(HashHelper.getHashedPassword(password, userInDb.getPasswordSalt()));
        baseDao.update(userInDb);
    }

    @Override
    public void saveUser(User user) {
        User param = new User();
        param.setUserName(user.getUserName());
        User userInDb = baseDao.getUnique(User.class, param);
        if (!ObjectHelper.isEmpty(userInDb)) {
            throw new ServiceException("username exists.");
        }
        baseDao.save(user);
    }

}
