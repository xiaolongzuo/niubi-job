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
        <div id="breadcrumb"><a href="${pageContext.request.contextPath}/standbyDashboard/index" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Home</a></div>
    </div>
    <div class="container-fluid">
        <div class="row-fluid">
            ${message}
            <div class="widget-box">
                <div class="widget-title"><span class="icon"><i class="icon-th"></i></span>
                    <h5>Job Manager</h5>
                </div>
                <div class="widget-content nopadding">
                    <table class="table table-bordered data-table">
                        <thead>
                            <tr>
                                <th>Mode</th>
                                <th>Method</th>
                                <th>Jar file name</th>
                                <th>Upload time</th>
                            </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${jobs}" var="job">
                            <tr class="gradeA">
                                <td><span class="label ${job.modeLabelClass}">${job.containerType}</span></td>
                                <td><a href="#" class="btn btn-mini btn-info">${job.groupName}.${job.jobName}</a></td>
                                <td><a href="#" class="btn btn-mini btn-inverse">${job.jarFileName}</a></td>
                                <td>${job.createDateString}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>