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
  Time: 16/1/16 05:00
--%>
<div id="content">
    <div id="content-header">
        <div id="breadcrumb"><a href="${pageContext.request.contextPath}/standbyDashboard/index" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Home</a></div>
    </div>
    <div class="container-fluid">
        <hr>
        <div class="row-fluid">
            ${message}
            <div class="span12">
                <div class="widget-box">
                    <div class="widget-title"><span class="icon"> <i class="icon-align-justify"></i> </span>
                        <h5>Upload jar</h5>
                    </div>
                    <div class="widget-content nopadding">
                        <form id="jar_upload" action="${pageContext.request.contextPath}/standbyJobs/upload" method="POST" class="form-horizontal" enctype="multipart/form-data">
                            <div class="control-group">
                                <label class="control-label">File upload input</label>
                                <div class="controls">
                                    <input name="jobJar" type="file"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label">Packages to scan :</label>
                                <div class="controls">
                                    <input name="packagesToScan" type="text" class="span6" placeholder="This is important!" />
                                </div>
                            </div>
                            <div class="form-actions">
                                <button type="submit" class="btn btn-success">Upload</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
