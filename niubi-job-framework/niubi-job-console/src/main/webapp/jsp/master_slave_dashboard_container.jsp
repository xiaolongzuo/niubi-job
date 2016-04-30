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
  Time: 16/1/15 02:04
--%>

<!--main-container-part-->
<div id="content">
    <!--breadcrumbs-->
    <div id="content-header">
        <div id="breadcrumb"> <a href="${pageContext.request.contextPath}/" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Home</a></div>
    </div>
    <!--End-breadcrumbs-->

    <!--Action boxes-->
    <div class="container-fluid">
        <div class="quick-actions_homepage">
            <ul class="quick-actions">
                <li class="bg_lb span4"> <a href="${pageContext.request.contextPath}/masterSlaveJobs"> <i class="icon-dashboard"></i> Job manager </a> </li>
                <li class="bg_lg span4"> <a href="${pageContext.request.contextPath}/masterSlaveNodes"> <i class="icon-signal"></i> Node manager</a> </li>
                <li class="bg_ly span4"> <a href="${pageContext.request.contextPath}/masterSlaveJobSummaries"> <i class="icon-inbox"></i> <!-- <span class="label label-success">101</span>--> Job runtime manager </a> </li>
                <li class="bg_lo span4"> <a href="${pageContext.request.contextPath}/masterSlaveJobs/upload"> <i class="icon-th"></i> Upload jar</a> </li>
                <li class="bg_ls span4"> <a href="${pageContext.request.contextPath}/masterSlaveJobLogs"> <i class="icon-fullscreen"></i> Operation log</a> </li>
                <!--
                <li class="bg_lo span3"> <a href="form-common.html"> <i class="icon-th-list"></i> Forms</a> </li>
                <li class="bg_ls"> <a href="buttons.html"> <i class="icon-tint"></i> Buttons</a> </li>
                <li class="bg_lb"> <a href="interface.html"> <i class="icon-pencil"></i>Elements</a> </li>
                <li class="bg_lg"> <a href="calendar.html"> <i class="icon-calendar"></i> Calendar</a> </li>
                <li class="bg_lr"> <a href="error_404.html"> <i class="icon-info-sign"></i> Error</a> </li>
                -->
            </ul>
        </div>
        <!--End-Action boxes-->
    </div>
</div>

<!--end-main-container-part-->
