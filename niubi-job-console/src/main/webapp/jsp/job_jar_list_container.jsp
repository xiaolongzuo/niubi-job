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
  Time: 16/1/16 17:37
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
                    <h5>Job Jar Manager</h5>
                </div>
                <div class="widget-content nopadding">
                    <table class="table table-bordered data-table">
                        <thead>
                        <tr>
                            <th>File name</th>
                            <th>Packages to scan</th>
                            <th>Upload time</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${jobJars}" var="jobJar">
                            <tr class="gradeA">
                                <td>${jobJar.jarFileName}</td>
                                <td>${jobJar.packagesToScan}</td>
                                <td>${jobJar.createDateString}</td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
