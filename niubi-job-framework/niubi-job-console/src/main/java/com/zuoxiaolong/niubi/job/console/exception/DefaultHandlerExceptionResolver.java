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

package com.zuoxiaolong.niubi.job.console.exception;

import lombok.Setter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class DefaultHandlerExceptionResolver implements HandlerExceptionResolver {

    @Setter
    private String defaultView;

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception exception) {
        handleException(httpServletRequest, exception);
        if (!(handler instanceof HandlerMethod)) {
            return getDefaultView();
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        ExceptionForward methodExceptionForward = handlerMethod.getMethodAnnotation(ExceptionForward.class);
        ExceptionForward beanExceptionForward = handlerMethod.getBeanType().getDeclaredAnnotation(ExceptionForward.class);
        StringBuffer stringBuffer = new StringBuffer("forward:");
        if (methodExceptionForward != null) {
            stringBuffer.append(methodExceptionForward.value());
            return new ModelAndView(stringBuffer.toString());
        }
        if (beanExceptionForward != null) {
            stringBuffer.append(beanExceptionForward.value());
            return new ModelAndView(stringBuffer.toString());
        }
        return getDefaultView();
    }

    protected ModelAndView getDefaultView() {
        return new ModelAndView("forward:" + defaultView);
    }

    protected void handleException(HttpServletRequest httpServletRequest, Exception exception) {
        httpServletRequest.setAttribute("message", "<div class=\"alert alert-error alert-block\">" +
                " <a class=\"close\" data-dismiss=\"alert\" href=\"#\">×</a>" +
                "<h4 class=\"alert-heading\">Error!</h4>" + exception.getClass().getName() + ":" +exception.getMessage() + "</div>");
    }

}
