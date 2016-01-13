package com.zuoxiaolong.niubi.job.console.main.controller;
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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.zuoxiaolong.niubi.job.api.view.NodeView;
import com.zuoxiaolong.niubi.job.console.main.service.NodeService;

@Controller
@RequestMapping("/node")
public class BasicController {
	
	@Resource
	private NodeService nodeService;
	
	@ResponseBody
	@RequestMapping(value = "/list")
	public List<NodeView> getAllNodes(){
		return nodeService.getAllNodes();
	}
	
	@RequestMapping(value = "/view")
	public ModelAndView index(){
		return new ModelAndView("nodeview");
	}
	
}