package com.zuoxiaolong.niubi.job.persistent;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

public class Pager<E> {

	private int pageNumber = 1;

	private int pageSize = 10;

	private int totalCount;
	
	private ViewJsonData<E> viewJsonData = new ViewJsonData<E>();

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