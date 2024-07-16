package com.davit.springblog.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.TokenRequestDto;
import com.davit.springblog.dto.UserDto;
import com.davit.springblog.dto.UserRequestDto;
import com.davit.springblog.dto.UserResponseDto;

public interface UserService {

	UserResponseDto register(UserRequestDto userRequestDto) throws IOException;
	
	UserResponseDto login(UserRequestDto userRequestDto);
	
	UserResponseDto refreshToken(TokenRequestDto tokenRequestDto);
	
	UserDto getMyProfile(String email);
	
	UserResponseDto update(Integer userId,UserRequestDto userRequestDto, MultipartFile file) throws IOException;
	
	PaginationResponseDto<UserDto> getAllUsers(String keyword, int pageNumber, int pageSize);

	void delete(Integer userId) throws IOException;
}
