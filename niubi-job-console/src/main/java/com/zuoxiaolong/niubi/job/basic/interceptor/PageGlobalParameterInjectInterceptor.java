package com.zuoxiaolong.niubi.job.basic.interceptor;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 将页面所需全局参数注入拦截器
 * 
 * 用于将全局参数，如 contextPath、basePath 注入到当前的 request 上下文中，以用于后续的 jsp 页面渲染过程
 * 
 */
public class PageGlobalParameterInjectInterceptor extends HandlerInterceptorAdapter {

	@Override
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
			final Object arg2, final ModelAndView mav) throws Exception {
		// 当在 Controller 中直接返回数据，不需要 jsp 等页面渲染的时候，mav 为空，不处理
		if (mav == null)
			return;
		
		if (isRedirectView(mav))
			return;
		
		final String contextPath = request.getContextPath() + "/";
		mav.addObject("contextPath", contextPath);
		
		final String basePath = request.getScheme() + "://"
				+ request.getServerName() + ":" + request.getServerPort()
				+ contextPath;
		
		mav.addObject("basePath", basePath);
	}

	private boolean isRedirectView(ModelAndView mav) {
		final View view = mav.getView();
		if (view != null && view instanceof RedirectView)
			return true;
		
		final String viewName = mav.getViewName();
		if (viewName != null && viewName.startsWith("redirect"))
			return true;
		
		return false;
	}

}
