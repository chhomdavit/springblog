package com.davit.springblog.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserDto {
	
	private Integer userId;
    private String email;
    private String name;
    private String imageProfile;
    private String password;
    private String tel;
    private int attempt;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String role;
}
