package com.zuoxiaolong.niubi.job.console.main.service;
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

import com.zuoxiaolong.niubi.job.api.view.NodeView;

public interface NodeService {
	
	/**
	 * get all available nodes
	 * @return the list of  NodeView
	 */
	List<NodeView> getAllNodes();
	
}
