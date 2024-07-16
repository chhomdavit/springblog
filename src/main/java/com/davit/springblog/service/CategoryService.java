package com.davit.springblog.service;

import java.io.IOException;
import java.util.List;

import com.davit.springblog.dto.CategoryDto;
import com.davit.springblog.dto.PaginationResponseDto;

public interface CategoryService {

	CategoryDto createCategory (CategoryDto categoryDto, Integer userId) throws IOException;
	
	CategoryDto updateCategory (Integer categoryId, CategoryDto categoryDto, Integer userId) throws IOException;
	
	PaginationResponseDto<CategoryDto> getAllCategories(String keyword, int pageNumber, int pageSize);
	
	List<CategoryDto> getCategories ();
	
	void  deleteCategory (Integer categoryId, Integer userId);
}
