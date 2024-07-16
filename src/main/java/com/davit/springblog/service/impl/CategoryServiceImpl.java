package com.davit.springblog.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.davit.springblog.dto.CategoryDto;
import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.entity.Categories;
import com.davit.springblog.entity.Users;
import com.davit.springblog.execption.AlreadyExistsException;
import com.davit.springblog.execption.EmptyOrNotNullException;
import com.davit.springblog.execption.ResourceNotFoundException;
import com.davit.springblog.repository.CategoryRepository;
import com.davit.springblog.repository.UserRepository;
import com.davit.springblog.service.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto, Integer userId) {
    	
    	if (categoryDto.getTitle() == null || categoryDto.getTitle().isEmpty()) {
            throw new EmptyOrNotNullException("Category title cannot be null or empty");
        } else if (categoryRepository.existsByTitle(categoryDto.getTitle())) {
            throw new AlreadyExistsException("Category title already exists");
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("userId not found with id: " + userId));

        Categories category = modelMapper.map(categoryDto, Categories.class);
        category.setUsers(user);
        Categories saved = categoryRepository.save(category);
        CategoryDto response = modelMapper.map(saved, CategoryDto.class);
        return response;
    }

    @Override
    public PaginationResponseDto<CategoryDto> getAllCategories(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Categories> categoryPages = (keyword == null || keyword.trim().isEmpty()) 
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByTitleContainingIgnoreCase(keyword, pageable);

        List<CategoryDto> categoryResponseList = categoryPages.getContent().stream().map(category -> {
            CategoryDto responseDto = modelMapper.map(category, CategoryDto.class);
            responseDto.setUsers(category.getUsers());
            return responseDto;
        }).collect(Collectors.toList());

        PaginationResponseDto<CategoryDto> paginationResponse = new PaginationResponseDto<>();
        paginationResponse.setList(categoryResponseList);
        paginationResponse.setPageNumber(categoryPages.getNumber());
        paginationResponse.setPageSize(categoryPages.getSize());
        paginationResponse.setTotalElements(categoryPages.getTotalElements());
        paginationResponse.setTotalPages(categoryPages.getTotalPages());
        paginationResponse.setLast(categoryPages.isLast());

        return paginationResponse;
    }

	@Override
	public CategoryDto updateCategory(Integer categoryId, CategoryDto categoryDto, Integer userId) throws IOException {
		Categories existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("userId not found with id: " + userId));

        if (categoryDto.getTitle() != null && !categoryDto.getTitle().isEmpty()) {
            existingCategory.setTitle(categoryDto.getTitle());
            existingCategory.setDescription(categoryDto.getDescription());
            existingCategory.getUsers().equals(user);
        }

        Categories saved = categoryRepository.save(existingCategory);
        CategoryDto response = modelMapper.map(saved, CategoryDto.class);
        return response;
	}

	@Override
	public void deleteCategory(Integer categoryId, Integer userId) {
		Categories existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("userId not found with id: " + userId));

        if (!existingCategory.getUsers().equals(user)) {
            throw new ResourceNotFoundException("userId not found with id: " + userId);
        }

        this.categoryRepository.delete(existingCategory);
	}

	@Override
	public List<CategoryDto> getCategories() {
		// TODO Auto-generated method stub
		return null;
	}
    
}

