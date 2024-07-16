package com.davit.springblog.dto;

import java.util.List;

import com.davit.springblog.entity.Users;

import lombok.Data;

@Data
public class CategoryDto {

	private Integer categoryId;

	private String title;

	private String description;

	private long postCount;

	private Users users;
	
	private List<PostDto> postData;
}
