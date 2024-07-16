package com.davit.springblog.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.davit.springblog.constant.AppConstants;
import com.davit.springblog.dto.CategoryDto;
import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.entity.Users;
import com.davit.springblog.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CategoryController {
	
	private final CategoryService categoryService;

	@PostMapping("/admin/category")
    public ResponseEntity<CategoryDto> createCategory(
    		
        @RequestBody CategoryDto categoryDto,
        Authentication authentication ) throws IOException {
        Users users = (Users) authentication.getPrincipal();
        CategoryDto createdCategory = categoryService.createCategory(categoryDto, users.getUserId());
        return ResponseEntity.ok().body(createdCategory);
    }
	
	
	@GetMapping("/auth/get-all-categories")
    public ResponseEntity<PaginationResponseDto<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
            @RequestParam(required = false) String keyword
    ) {
        if (keyword != null && !keyword.trim().isEmpty() && pageNumber > 0) {
            pageNumber = 0;
        }
        PaginationResponseDto<CategoryDto> userResponse = categoryService.getAllCategories(keyword, pageNumber, pageSize);
        return ResponseEntity.ok().body(userResponse);
    }
	
	
	@PutMapping("/admin/category/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable(value = "categoryId") Integer categoryId,
            @RequestBody CategoryDto categoryDto,
            Authentication authentication
            ) throws IOException {
        Users users = (Users) authentication.getPrincipal();
        CategoryDto updatedCategory = categoryService.updateCategory(categoryId, categoryDto, users.getUserId());
        return ResponseEntity.ok().body(updatedCategory);
    }
	
	@DeleteMapping("/admin/category/{categoryId}")
    public ResponseEntity<String> deleteCategory(
        @PathVariable Integer categoryId,
        Authentication authentication
        ) {
        Users users = (Users) authentication.getPrincipal();
        categoryService.deleteCategory(categoryId, users.getUserId());
        return ResponseEntity.ok("Category deleted successfully");
    }
	
	@GetMapping("/auth/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> catDto = categoryService.getCategories();
        return ResponseEntity.ok(catDto);
    }
	
}
