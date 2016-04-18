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
        <div id="breadcrumb"><a href="${pageContext.request.contextPath}/standbyJobSummaries" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Job runtime manager</a></div>
    </div>
    <div class="container-fluid">
        <hr>
        <div class="row-fluid">
            ${message}
            <div class="span12">
                <div class="widget-box">
                    <div class="widget-title"><span class="icon"> <i class="icon-align-justify"></i> </span>
                        <h5>Edit Job : [${jobSummary.groupName}.${jobSummary.jobName}-->(${jobSummary.jobState})]</h5>
                    </div>
                    <div class="widget-content nopadding">
                        <form id="job_summary_update" action="${pageContext.request.contextPath}/standbyJobSummaries/update" method="POST" class="form-horizontal" enctype="multipart/form-data">
                            <input name="groupName" type="hidden" value="${jobSummary.groupName}" />
                            <input name="jobName" type="hidden" value="${jobSummary.jobName}" />
                            <input name="originalJarFileName" type="hidden" value="${jobSummary.originalJarFileName}" />
                            <div class="control-group">
                                <label class="control-label">Cron :</label>
                                <div class="controls">
                                    <input name="jobCron" type="text" class="span6" value="${jobSummary.jobCron}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Environment :</label>
                                <div class="controls">
                                    <select name="jarFileName" class="span8">
                                        <c:forEach items="${jarFileNameList}" var="jarFileName">
                                            <option value="${jarFileName}" <c:if test="${jarFileName == jobSummary.originalJarFileName}">selected</c:if>>
                                            ${jarFileName}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Misfire Policy :</label>
                                <div class="controls">
                                    <select name="misfirePolicy" class="span4">
                                        <option value="None" <c:if test="${jobSummary.misfirePolicy == 'None'}">selected</c:if>>None</option>
                                        <option value="DoNothing" <c:if test="${jobSummary.misfirePolicy == 'DoNothing'}">selected</c:if>>DoNothing</option>
                                        <option value="IgnoreMisfires" <c:if test="${jobSummary.misfirePolicy == 'IgnoreMisfires'}">selected</c:if>>IgnoreMisfires</option>
                                        <option value="FireAndProceed" <c:if test="${jobSummary.misfirePolicy == 'FireAndProceed'}">selected</c:if>>FireAndProceed</option>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Operation :</label>
                                <div class="controls">
                                    <select name="jobOperation" class="span4">
                                        <c:if test="${jobSummary.jobState == 'Shutdown' || jobSummary.jobState == 'Pause'}">
                                            <option value="Start">Start</option>
                                        </c:if>
                                        <c:if test="${jobSummary.jobState == 'Startup'}">
                                            <option value="Pause">Pause</option>
                                            <option value="Restart">Restart</option>
                                        </c:if>
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

