package com.davit.springblog.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.PostDto;
import com.davit.springblog.entity.Categories;
import com.davit.springblog.entity.Posts;
import com.davit.springblog.entity.Roles;
import com.davit.springblog.entity.Users;
import com.davit.springblog.execption.EmptyOrNotNullException;
import com.davit.springblog.execption.ResourceNotFoundException;
import com.davit.springblog.repository.CategoryRepository;
import com.davit.springblog.repository.PostRepository;
import com.davit.springblog.repository.UserRepository;
import com.davit.springblog.service.FileUploadService;
import com.davit.springblog.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
	
	private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final ModelMapper modelMapper;
    
    @Value("${project.upload}")
    private String path;
    @Value("${base.url}")
    private String baseUrl;

	@Override
	public PostDto createPost(PostDto postDto, MultipartFile file, Integer categoryId, Integer userId)
			throws IOException {
		// Check if title is null or empty
        if (postDto.getTitle() == null || postDto.getTitle().isEmpty()) {
            throw new EmptyOrNotNullException("Post title cannot be null or empty");
        }

        // Save file if provided
        String newFileName = null;
        if (file != null && !file.isEmpty()) {
            newFileName = fileUploadService.saveFile(file, path);
            postDto.setPostImage(newFileName);
        }

        // Retrieve category
        Categories categories = this.categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + categoryId));

        // Retrieve category
        Users users = this.userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Map DTO to entity
        Posts posts = modelMapper.map(postDto, Posts.class);
        posts.setUsers(users);
        posts.setCategories(categories);
   
        // Save post
        Posts saved = postRepository.save(posts);

        // Construct response DTO
        PostDto response = modelMapper.map(saved, PostDto.class);
        if (newFileName != null) {
            String postImageUrl = baseUrl + "/auth/" + newFileName;
            response.setPostImageUrl(postImageUrl);
        }
        return response;
	}

	@Override
	public PaginationResponseDto<PostDto> getAllPosts(String keyword, int pageNumber, int pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Posts> postPages;
        if (keyword == null || keyword.trim().isEmpty()) {
            postPages = postRepository.findAllByIsDeletedFalse(pageable); 
        } else {
            postPages = postRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
        }

        List<PostDto> postResponseList = postPages.getContent().stream().map(post -> {
                PostDto postDto = modelMapper.map(post, PostDto.class);
                postDto.setPostImageUrl(baseUrl + "/auth/" + post.getPostImage());
                return postDto;
            })
            .collect(Collectors.toList());

        PaginationResponseDto<PostDto> paginationResponse = new PaginationResponseDto<>();
		paginationResponse.setList(postResponseList);
		paginationResponse.setPageNumber(postPages.getNumber());
		paginationResponse.setPageSize(postPages.getSize());
		paginationResponse.setTotalElements(postPages.getTotalElements());
		paginationResponse.setTotalPages(postPages.getTotalPages());
		paginationResponse.setLast(postPages.isLast());

		return paginationResponse;
	}

	@Override
	public PostDto updatePost(Integer postId, PostDto postDto, MultipartFile file, Integer categoryId, Integer userId)
			throws IOException {
		  Users user = this.userRepository.findById(userId)
		            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
	              
	      Posts existingPost = postRepository.findById(postId)
	              .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

	      if (!existingPost.getUsers().equals(user)) {
	              throw new ResourceNotFoundException("User not found with id: " + userId);
	      }

	      String newSaveFile = null;
	      try {
	          if (file != null && !file.isEmpty()) {
	              if (existingPost.getPostImage() != null && !existingPost.getPostImage().isEmpty()) {
	                  Files.deleteIfExists(Paths.get(path, existingPost.getPostImage()));
	              }
	              newSaveFile = fileUploadService.saveFile(file, path);
	          } else {
	              newSaveFile = existingPost.getPostImage();
	          }
	      } catch (Exception e) {
	          e.printStackTrace();
	          throw new RuntimeException("Error occurred while updating post", e);
	      }
	      postDto.setPostImage(newSaveFile);

	      Posts updated = modelMapper.map(postDto, Posts.class);
	      updated.setPostId(existingPost.getPostId());
	      updated.setUsers(user);
	      if (categoryId != null) {
	          Categories categories = categoryRepository.findById(categoryId)
	                  .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
	          updated.setCategories(categories);
	      } else {
	          updated.setCategories(existingPost.getCategories());
	      }

	      Posts saved = postRepository.save(updated);
	      PostDto response = modelMapper.map(saved, PostDto.class);
	      if (newSaveFile != null) {
	          String postImageUrl = baseUrl + "/auth/" + newSaveFile;
	          response.setPostImageUrl(postImageUrl);
	      }
	      return response;
	}

	@Override
	public void deletePost(Integer postId, Integer userId) throws IOException {
		
		if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
		
		Users users = this.userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		
		Posts posts = postRepository.findById(postId)
	            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

	        if (!posts.getUsers().getUserId().equals(userId) && !users.getRole().equals(Roles.ADMIN)) {
	            throw new ResourceNotFoundException("User not found with id: " + userId);
	        }

	        String uploadDirectory = path;
	        String photoFileName = posts.getPostImage();

	        if (photoFileName != null && !photoFileName.isEmpty()) {
	            try {
	                Files.deleteIfExists(Paths.get(uploadDirectory, photoFileName));
	            } catch (IOException e) {
	                e.printStackTrace();
	                throw new IOException("Failed to delete photo", e);
	            }
	        }
	        postRepository.deleteById(postId);	
	}

	@Override
	public void softDeletePost(Integer postId, Integer userId) throws IOException {
		
		if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
		
		Users users = this.userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		
        Posts posts = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!posts.getUsers().getUserId().equals(userId) && !users.getRole().equals(Roles.ADMIN)) {
            throw new ResourceNotFoundException("Users not found with id: " + userId);
        }

        posts.setDeleted(true);
        postRepository.save(posts);
	}

	
}
