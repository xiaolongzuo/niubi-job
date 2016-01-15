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
  Time: 16/1/15 02:08
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
            <div class="widget-box">
                <div class="widget-title"><span class="icon"><i class="icon-th"></i></span>
                    <h5>Node Manager</h5>
                </div>
                <div class="widget-content nopadding">
                    <table class="table table-bordered data-table">
                        <thead>
                            <tr>
                                <th>State</th>
                                <th>Name</th>
                                <th>Cron</th>
                                <th>Mode</th>
                                <th>Group</th>
                                <th>Jar file</th>
                                <th>Misfire policy</th>
                                <th>Operation</th>
                            </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${jobs}" var="job">
                            <tr class="gradeA">
                                <td>
                                    ${job.stateLabel}
                                </td>
                                <td>${job.jobName}</td>
                                <td>${job.cron}</td>
                                <td>${job.modeLabel}</td>
                                <td>${job.groupName}</td>
                                <td>${job.jobJar.jarFileName}</td>
                                <td>${job.misfirePolicy}</td>
                                <td>
                                    <button class="btn">Restart</button>
                                    <button data-toggle="dropdown" class="btn dropdown-toggle"><span class="caret"></span></button>
                                    <ul class="dropdown-menu">
                                        <li><a href="#">Restart</a></li>
                                        <li><a href="#">Shutdown</a></li>
                                        <li><a href="#">Pause</a></li>
                                    </ul>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>