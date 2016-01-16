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
  Time: 16/1/16 18:44
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="content">
    <div id="content-header">
        <div id="breadcrumb"><a href="/job" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Job manager</a></div>
    </div>
    <div class="container-fluid">
        <hr>
        <div class="row-fluid">
            <div class="span6">
                <div class="widget-box">
                    <div class="widget-title"><span class="icon"> <i class="icon-align-justify"></i> </span>
                        <h5>Edit Job : [${job.groupName}.${job.jobName}]</h5>
                    </div>
                    <div class="widget-content nopadding">
                        <form action="/job" method="POST" class="form-horizontal" enctype="multipart/form-data">
                            <input name="id" type="hidden" value="${job.id}" />
                            <input name="groupName" type="hidden" value="${job.groupName}" />
                            <input name="jobName" type="hidden" value="${job.jobName}" />
                            <div class="control-group">
                                <label class="control-label">Cron :</label>
                                <div class="controls">
                                    <input name="cron" type="text" class="span10" value="${job.cron}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Environment :</label>
                                <div class="controls">
                                    <select name="jarFileName" class="span11">
                                        <c:forEach items="${sameGroupAndNameJobs}" var="sameGroupAndNameJob">
                                            <option value="${sameGroupAndNameJob.jobJar.jarFileName}">${sameGroupAndNameJob.jobJar.jarFileName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Mode :</label>
                                <div class="controls">
                                    <select name="mode" class="span11">
                                        <option value="COMMON">COMMON</option>
                                        <option value="SPRING">SPRING</option>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Misfire Policy :</label>
                                <div class="controls">
                                    <select name="mode" class="span11">
                                        <option value="None">None</option>
                                        <option value="DoNothing">DoNothing</option>
                                        <option value="IgnoreMisfires">IgnoreMisfires</option>
                                        <option value="FireAndProceed">FireAndProceed</option>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Operation :</label>
                                <div class="controls">
                                    <select name="operation" class="span11">
                                        <option value="Start">Start</option>
                                        <option value="Shutdown">Shutdown</option>
                                        <option value="Restart">Restart</option>
                                    </select>
                                </div>
                            </div>
                            <div class="form-actions">
                                <button type="submit" class="btn btn-success">Execute</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

