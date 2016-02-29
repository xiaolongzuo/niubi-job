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

package com.zuoxiaolong.niubi.job.persistent;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Xiaolong Zuo
 * @since 0.9.3
 */
public class Pager<E> {

	private int pageNumber = 1;

	private int pageSize = 10;

	private int totalCount;
	
	private ViewJsonData<E> viewJsonData = new ViewJsonData<>();

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		viewJsonData.setTotal(totalCount);
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getTotalPage() {
		int result = totalCount / pageSize;
        return (totalCount % pageSize == 0) ? result : (result + 1); 
	}
	
	public int getFirstIndex() {  
        return pageSize * (pageNumber - 1);  
    }  
  
    public boolean hasPrevious() {  
        return pageNumber > 1;  
    }  
  
    public boolean hasNext() {  
        return pageNumber < getTotalPage();  
    }

	public void setDataList(List<E> dataList) {
		viewJsonData.setRows(dataList);
	}  
	
	public ViewJsonData<E> getViewJsonData(){
		return viewJsonData;
	}
	
	@XmlRootElement
	public static class ViewJsonData<E> {
		
		private int total;
		
		private List<E> rows;

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public List<E> getRows() {
			return rows;
		}

		public void setRows(List<E> rows) {
			this.rows = rows;
		}
		
	}
}