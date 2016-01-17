<%--

    Copyright 2002-2016 the original author or authors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
--%>
<%--
  User: Xiaolong Zuo
  Time: 16/1/17 00:52
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="content">
    <!--breadcrumbs-->
    <div id="content-header">
        <div id="breadcrumb"><a href="/" title="Go to Home" class="tip-bottom"><i
                class="icon-home"></i> Home</a></div>
    </div>
    <div class="container-fluid">
        <div class="row-fluid">
            ${operationSuccess}
            <div class="widget-box">
                <div class="widget-title"><span class="icon"><i class="icon-th"></i></span>
                    <h5>Job Manager</h5>
                </div>
                <div class="widget-content nopadding">
                    <table class="table table-bordered data-table">
                        <thead>
                        <tr>
                            <th>State</th>
                            <th>Method</th>
                            <th>Mode</th>
                            <th>Cron</th>
                            <th>Jar file name</th>
                            <th>Modify time</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${jobRuntimeDetails}" var="jobRuntimeDetail">
                            <tr class="gradeA">
                                <td><span class="label ${jobRuntimeDetail.stateLabelClass}">${jobRuntimeDetail.state}</span></td>
                                <td><a href="/jobRuntimeDetails/${jobRuntimeDetail.id}">${jobRuntimeDetail.groupName}.${jobRuntimeDetail.jobName}</a></td>
                                <td><span class="label ${jobRuntimeDetail.modeLabelClass}">${jobRuntimeDetail.mode}</span></td>
                                <td>${jobRuntimeDetail.cron}</td>
                                <td>${jobRuntimeDetail.jarFileName}</td>
                                <td>${jobRuntimeDetail.modifyDateString}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
