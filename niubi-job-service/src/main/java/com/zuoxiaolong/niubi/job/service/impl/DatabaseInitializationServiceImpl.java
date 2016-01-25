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

package com.zuoxiaolong.niubi.job.service.impl;

import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.persistent.BaseDao;
import com.zuoxiaolong.niubi.job.persistent.entity.Role;
import com.zuoxiaolong.niubi.job.persistent.entity.User;
import com.zuoxiaolong.niubi.job.service.DatabaseInitializationService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Xiaolong Zuo
 * @since 16/1/26 00:41
 */
@Component
public class DatabaseInitializationServiceImpl implements DatabaseInitializationService {

    @Autowired
    private BaseDao baseDao;

    @Override
    public void initialize() {
        String adminUsername = "admin";
        String adminPassword = "123456";
        String hashAlgorithm = "MD5";

        User param = new User();
        param.setUsername(adminUsername);
        User admin = baseDao.getUnique(User.class, param);
        if (admin != null) {
            LoggerHelper.info("database has been initialized.");
            return;
        }
        LoggerHelper.info("begin init database.");
        admin = new User();
        admin.setUsername(adminUsername);
        Hash hash = new SimpleHash(hashAlgorithm, adminPassword, adminUsername);
        admin.setPassword(hash.toHex());
        admin.setPasswordSalt(adminUsername);

        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role.setDescription("Administrator");
        baseDao.save(role);

        admin.setRoleList(Arrays.asList(role));
        baseDao.save(admin);

        LoggerHelper.info("init database successfully.");
    }

}
