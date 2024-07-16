package com.davit.springblog.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.PostDto;

public interface PostService {

	PostDto createPost(PostDto postDto ,MultipartFile file ,Integer categoryId, Integer userId) throws IOException;
	
	PaginationResponseDto<PostDto> getAllPosts(String keyword, int pageNumber, int pageSize);
	
	PostDto updatePost(Integer postId, PostDto postDto, MultipartFile file ,Integer categoryId ,Integer userId) throws IOException;
			
	void deletePost(Integer postId ,Integer userId) throws IOException;

	void softDeletePost(Integer postId ,Integer userId) throws IOException;
}
