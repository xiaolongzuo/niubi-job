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
import com.zuoxiaolong.niubi.job.persistent.shiro.HashHelper;
import com.zuoxiaolong.niubi.job.service.DatabaseInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 数据库初始化服务实现类
 *
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Component
public class DatabaseInitializationServiceImpl implements DatabaseInitializationService {

    @Autowired
    private BaseDao baseDao;

    /**
     * 本方法先检查数据库是否有admin为username的用户,如果没有则会插入一条,并且赋予权限.
     */
    @Override
    public void initialize() {
        String adminUsername = "admin";
        String adminPassword = "123456";

        User param = new User();
        param.setUserName(adminUsername);
        User admin = baseDao.getUnique(User.class, param);
        if (admin != null) {
            LoggerHelper.info("database has been initialized.");
            return;
        }
        LoggerHelper.info("begin init database.");
        admin = new User();
        admin.setUserName(adminUsername);
        admin.setUserPassword(HashHelper.getHashedPassword(adminPassword, adminUsername));
        admin.setPasswordSalt(adminUsername);

        Role role = new Role();
        role.setRoleName("ROLE_ADMIN");
        role.setRoleDescription("Administrator");
        baseDao.save(role);

        admin.setRoleList(Arrays.asList(role));
        baseDao.save(admin);

        LoggerHelper.info("init database successfully.");
    }

}
