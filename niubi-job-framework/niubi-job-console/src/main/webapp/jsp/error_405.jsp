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
  Time: 16/1/15 01:54
--%>
<%@ page contentType="text/html;charset=utf-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="dashboard_head.jsp"/>
</head>
<body>
<jsp:include page="dashboard_header.jsp"/>
<jsp:include page="dashboard_top.jsp"/>
<jsp:include page="dashboard_sidebar.jsp"/>
<div id="content">
    <div id="content-header">
        <div id="breadcrumb"> <a href="#" title="Go to Home" class="tip-bottom"><i class="icon-home"></i> Home</a> <a href="#">Sample pages</a> <a href="#" class="current">Error</a> </div>
        <h1>Error 405</h1>
    </div>
    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span12">
                <div class="widget-box">
                    <div class="widget-title"> <span class="icon"> <i class="icon-info-sign"></i> </span>
                        <h5>Error 405</h5>
                    </div>
                    <div class="widget-content">
                        <div class="error_ex">
                            <h1>405</h1>
                            <h3>Something is wrong here. </h3>
                            <p>Method not allowed!</p>
                            <a class="btn btn-warning btn-big"  href="/">Back to Home</a> </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<jsp:include page="dashboard_footer.jsp"/>
<jsp:include page="dashboard_bottom.jsp"/>
</body>
</html>
