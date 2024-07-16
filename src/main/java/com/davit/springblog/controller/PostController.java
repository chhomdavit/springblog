package com.davit.springblog.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.constant.AppConstants;
import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.PostDto;
import com.davit.springblog.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping("/auth/user/{userId}/post")
	public ResponseEntity<PostDto> createPost(@PathVariable Integer userId, @RequestParam("title") String title,
			@RequestParam("content") String content, @RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam("categoryId") Integer categoryId) {
		PostDto postDto = new PostDto();
		postDto.setTitle(title);
		postDto.setContent(content);
		postDto.setUpdated(LocalDateTime.now());
		postDto.setCreated(LocalDateTime.now());
		postDto.setDeleted(false);
		try {
			PostDto createdPost = postService.createPost(postDto, file, categoryId, userId);
			return ResponseEntity.ok().body(createdPost);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/auth/get-all-post")
	public ResponseEntity<PaginationResponseDto<PostDto>> getAllPost(@RequestParam String keyword,
			@RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize) {

		if (keyword != null && !keyword.trim().isEmpty() && pageNumber > 0) {
			pageNumber = 0;
		}
		PaginationResponseDto<PostDto> postResponse = postService.getAllPosts(keyword, pageNumber, pageSize);
		return ResponseEntity.ok().body(postResponse);
	}

	@PutMapping("/auth/user/{userId}/post/{postId}")
	public ResponseEntity<PostDto> updatePoast(@PathVariable(value = "postId") Integer postId,
			@PathVariable(value = "userId") Integer userId, @RequestParam("title") String title,
			@RequestParam("content") String content, @RequestParam(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "categoryId", required = false) Integer categoryId) {

		PostDto postDto = new PostDto();
		postDto.setTitle(title);
		postDto.setContent(content);
		postDto.setUpdated(LocalDateTime.now());
		postDto.setCreated(LocalDateTime.now());

		try {
			PostDto updatedPostDto = postService.updatePost(postId, postDto, file, categoryId, userId);
			return ResponseEntity.ok().body(updatedPostDto);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/auth/user/{userId}/post/{postId}")
    public ResponseEntity<String> deletePost(
        @PathVariable(value = "postId") Integer postId,
        @PathVariable(value = "userId") Integer userId
        ) {
        try {
            postService.deletePost(postId , userId);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete Post: " + e.getMessage());
        }
    }

    @DeleteMapping("auth/soft-delete/user/{userId}/post/{postId}")
    public ResponseEntity<String> softDeletePost(
        @PathVariable(value = "postId") Integer postId,
        @PathVariable(value = "userId") Integer userId
        ) {
    	try {
    		postService.softDeletePost(postId ,userId);
            return ResponseEntity.ok("Post deleted successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete Post: " + e.getMessage());
        }
    }
}
