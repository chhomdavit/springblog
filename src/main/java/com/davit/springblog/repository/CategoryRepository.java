package com.davit.springblog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.davit.springblog.entity.Categories;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {
	
    Page<Categories> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    boolean existsByTitle(String title);
}