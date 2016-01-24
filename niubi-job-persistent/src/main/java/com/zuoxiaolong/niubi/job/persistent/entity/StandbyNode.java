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


package com.zuoxiaolong.niubi.job.persistent.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author Xiaolong Zuo
 * @since 1/15/2016 12:02
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(uniqueConstraints = {@UniqueConstraint(name = "UNIQUE_STANDBY_NODE", columnNames = {"identifier"})})
public class StandbyNode extends AbstractNode {

    public String getStateLabelClass() {
        if ("Master".equals(getState())) {
            return "label-important";
        }
        if ("Backup".equals(getState())) {
            return "label-info";
        }
        return "";
    }

}
