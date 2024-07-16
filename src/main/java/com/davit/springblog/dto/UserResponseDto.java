package com.davit.springblog.dto;

import java.util.List;

import com.davit.springblog.entity.Users;

import lombok.Data;

@Data
public class UserResponseDto {

	  private List<Users> usersList;
	  
	  private String accessToken;
	  
	  private String refreshToken;
}
