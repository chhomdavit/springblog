package com.davit.springblog.dto;

import java.util.List;

import lombok.Data;

@Data
public class PaginationResponseDto<T> {
	
	  private List<T> list;

	  private Integer pageNumber;
	  
	  private Integer pageSize;
	  
	  private Long totalElements;
	  
	  private int totalPages;
	  
	  private  boolean isLast;
}
