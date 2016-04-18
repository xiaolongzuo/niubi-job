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

package com.zuoxiaolong.niubi.job.persistent.entity;

import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "job_role", uniqueConstraints = {@UniqueConstraint(name = "UNIQUE_ROLE", columnNames = {"role_name"})})
public class Role extends AbstractEntity {

    private String roleName;

    private String roleDescription;

    private List<User> userList;

    private List<Permission> permissionList;

    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roleList")
    public List<User> getUserList() {
        return userList;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="role_permission",joinColumns={@JoinColumn(name="role_id")}, inverseJoinColumns={@JoinColumn(name="permission_id")})
    public List<Permission> getPermissionList() {
        return permissionList;
    }

}
