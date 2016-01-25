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
  Time: 16/1/15 02:03
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--sidebar-menu-->
<div id="sidebar"><a href="#" class="visible-phone"><i class="icon icon-home"></i> Dashboard</a>
    <ul>
        <li> <a href="${pageContext.request.contextPath}/dashboard/index"><i class="icon icon-home"></i> <span>Switch Mode</span></a> </li>
        <li class="${param.standbyDashboardIndex}"> <a href="${pageContext.request.contextPath}/standbyDashboard/index"><i class="icon icon-dashboard"></i> <span>Dashboard</span></a> </li>
        <li class="${param.standbyNodes}"> <a href="${pageContext.request.contextPath}/standbyNodes"><i class="icon icon-cog"></i> <span>Node manager</span></a> </li>
        <li class="${param.standbyJobSummaries}"> <a href="${pageContext.request.contextPath}/standbyJobSummaries"><i class="icon icon-inbox"></i> <span>Job runtime manager</span></a> </li>
        <li class="${param.standbyJobLogs}"><a href="${pageContext.request.contextPath}/standbyJobLogs"><i class="icon icon-th"></i> <span>Operation log</span></a></li>
        <li class="${param.standbyJobsUpload}"> <a href="${pageContext.request.contextPath}/standbyJobs/upload"><i class="icon icon-inbox"></i> <span>Upload jar</span></a> </li>
        <li class="${param.standbyJobs}"> <a href="${pageContext.request.contextPath}/standbyJobs"><i class="icon icon-signal"></i> <span>Job manager</span></a> </li>


        <!--
        <li><a href="grid.html"><i class="icon icon-fullscreen"></i> <span>Full width</span></a></li>
        <li class="submenu"> <a href="#"><i class="icon icon-th-list"></i> <span>Forms</span> <span class="label label-important">3</span></a>
            <ul>
                <li><a href="form-common.html">Basic Form</a></li>
                <li><a href="form-validation.html">Form with Validation</a></li>
                <li><a href="form-wizard.html">Form with Wizard</a></li>
            </ul>
        </li>
        <li><a href="buttons.html"><i class="icon icon-tint"></i> <span>Buttons &amp; icons</span></a></li>
        <li><a href="interface.html"><i class="icon icon-pencil"></i> <span>Eelements</span></a></li>
        <li class="submenu"> <a href="#"><i class="icon icon-file"></i> <span>Addons</span> <span class="label label-important">5</span></a>
            <ul>
                <li><a href="index2.html">Dashboard2</a></li>
                <li><a href="gallery.html">Gallery</a></li>
                <li><a href="calendar.html">Calendar</a></li>
                <li><a href="invoice.html">Invoice</a></li>
                <li><a href="chat.html">Chat option</a></li>
            </ul>
        </li>
        <li class="submenu"> <a href="#"><i class="icon icon-info-sign"></i> <span>Error</span> <span class="label label-important">4</span></a>
            <ul>
                <li><a href="error403.html">Error 403</a></li>
                <li><a href="error404.html">Error 404</a></li>
                <li><a href="error405.html">Error 405</a></li>
                <li><a href="error500.html">Error 500</a></li>
            </ul>
        </li>
        <li class="content"> <span>Monthly Bandwidth Transfer</span>
            <div class="progress progress-mini progress-danger active progress-striped">
                <div style="width: 77%;" class="bar"></div>
            </div>
            <span class="percent">77%</span>
            <div class="stat">21419.94 / 14000 MB</div>
        </li>
        <li class="content"> <span>Disk Space Usage</span>
            <div class="progress progress-mini active progress-striped">
                <div style="width: 87%;" class="bar"></div>
            </div>
            <span class="percent">87%</span>
            <div class="stat">604.44 / 4000 MB</div>
        </li>
        -->
    </ul>
</div>
<!--sidebar-menu-->
