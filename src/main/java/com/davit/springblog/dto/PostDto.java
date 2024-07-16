package com.davit.springblog.dto;

import java.time.LocalDateTime;

import com.davit.springblog.entity.Users;

import lombok.Data;

@Data
public class PostDto {


	private Integer postId;

	private String title;

	private String content;

	private String postImage;
	
	private String postImageUrl;

    private LocalDateTime created;
    
    private LocalDateTime updated;

	private boolean isDeleted = false;

	private CategoryDto categories;

	private Users users;
}
