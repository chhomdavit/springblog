package com.davit.springblog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.davit.springblog.entity.Categories;
import com.davit.springblog.entity.Posts;

public interface PostRepository extends JpaRepository<Posts, Integer>{

	List<Posts> findByCategories(Categories categories);
	
	Page<Posts> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Posts> findAllByIsDeletedFalse(Pageable pageable);

    Page<Posts> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String keyword, Pageable pageable);
}
