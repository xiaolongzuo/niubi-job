/*
 * Copyright 2002-2015 the original author or authors.
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

import com.zuoxiaolong.niubi.job.console.exception.ExceptionForward;
import com.zuoxiaolong.niubi.job.core.helper.LoggerHelper;
import com.zuoxiaolong.niubi.job.service.UserService;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
@Controller
@RequestMapping("/shiro")
public class ShiroController extends AbstractController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login")
    public String login() {
        return "shiro_login";
    }

    @RequestMapping(value = "/password")
    public String password() {
        return "shiro_password";
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public String changePassword(String password) {
        String username = getUsernameAndCheck();
        userService.updatePassword(username, password);
        return success("/dashboard/index");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ExceptionForward("/shiro/login")
    public String login(HttpServletRequest request) {
        String exception = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        if (UnknownAccountException.class.getName().equals(exception)) {
            failed("Unknown account.");
        } else if (IncorrectCredentialsException.class.getName().equals(exception)) {
            failed("Incorrect password.");
        } else {
            LoggerHelper.error("unknown error : " + exception);
            failed("Unknown error.");
        }
        return "shiro_login";
    }

}
