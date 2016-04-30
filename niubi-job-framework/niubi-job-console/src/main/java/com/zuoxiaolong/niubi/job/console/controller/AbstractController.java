/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zuoxiaolong.niubi.job.console.controller;

import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public abstract class AbstractController {

    protected HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    protected HttpSession getSession() {
        return getRequest().getSession();
    }

    protected String getUsername() {
        HttpSession session = getSession();
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userName");
    }

    protected String getUsernameAndCheck() {
        String username = getUsername();
        if (username == null) {
            throw new UnknownAccountException();
        }
        return username;
    }

    protected String success(String url) {
        getRequest().setAttribute("message",
                "<div class=\"alert alert-success alert-block\"> " +
                        "<a class=\"close\" data-dismiss=\"alert\" href=\"#\">×</a>" +
                        "<h4 class=\"alert-heading\">Success!</h4>Operation successfully!</div>");
        return "forward:" + url;
    }

    protected void failed(String message) {
        getRequest().setAttribute("message", message);
    }

    protected String getDirectoryRealPath(String path) {
        String dirPath = getRequest().getServletContext().getRealPath(path);
        if (dirPath.endsWith("/")) {
            dirPath = dirPath.substring(0, dirPath.length() - 1);
        }
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dirPath;
    }

}
