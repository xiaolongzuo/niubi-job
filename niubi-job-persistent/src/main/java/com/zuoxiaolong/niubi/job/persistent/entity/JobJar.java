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

import com.zuoxiaolong.niubi.job.core.helper.DateHelper;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 16/1/15 23:28
 */
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
public class JobJar extends BaseEntity {

    private String jarFileName;

    private String packagesToScan;

    private List<Job> jobs;

    public JobJar() {
    }

    public JobJar(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String getPackagesToScan() {
        return packagesToScan;
    }

    @Column(unique = true)
    public String getJarFileName() {
        return jarFileName;
    }

    @OneToMany(mappedBy = "jobJar", fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    public List<Job> getJobs() {
        return jobs;
    }

    @Transient
    public String getCreateDateString() {
        return DateHelper.format(getCreateDate());
    }

}
