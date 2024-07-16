package com.davit.springblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.constant.AppConstants;
import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.TokenRequestDto;
import com.davit.springblog.dto.UserDto;
import com.davit.springblog.dto.UserRequestDto;
import com.davit.springblog.dto.UserResponseDto;
import com.davit.springblog.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/auth/register")
	public ResponseEntity<UserResponseDto> register(@RequestParam("email") String email,
			@RequestParam("name") String name, @RequestParam("password") String password) {

		UserRequestDto userRequestDto = new UserRequestDto();
		userRequestDto.setEmail(email);
		userRequestDto.setName(name);
		userRequestDto.setPassword(password);

		try {
			UserResponseDto userResponseDto = userService.register(userRequestDto);
			return ResponseEntity.ok().body(userResponseDto);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/auth/login")
	public ResponseEntity<UserResponseDto> login(@RequestBody UserRequestDto userRequestDto) {
		return ResponseEntity.ok(userService.login(userRequestDto));
	}

	@PostMapping("/auth/refresh")
	public ResponseEntity<UserResponseDto> refreshToken(@RequestBody TokenRequestDto tokenRequestDto) {
		return ResponseEntity.ok(userService.refreshToken(tokenRequestDto));
	}

	@GetMapping("/auth/get-profile")
	public ResponseEntity<UserDto> getMyProfile() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		UserDto response = userService.getMyProfile(email);
		return ResponseEntity.ok().body(response);
	}

	@PutMapping("/auth/update/{userId}")
	public ResponseEntity<UserResponseDto> update(@PathVariable Integer userId, @RequestParam("name") String name,
			@RequestParam("email") String email, @RequestParam("tel") String tel, @RequestParam("role") String role,
			@RequestParam("password") String password,
			@RequestParam(value = "file", required = false) MultipartFile file) {

		UserRequestDto userRequestDto = new UserRequestDto();
		userRequestDto.setName(name);
		userRequestDto.setEmail(email);
		userRequestDto.setTel(tel);
		userRequestDto.setRole(role);
		userRequestDto.setPassword(password);

		try {
			UserResponseDto updatedUserDto = userService.update(userId, userRequestDto, file);
			return ResponseEntity.ok().body(updatedUserDto);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/auth/get-all-users")
	public ResponseEntity<PaginationResponseDto<UserDto>> getAllUsers(
			@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) int pageNumber,
			@RequestParam(defaultValue = AppConstants.PAGE_SIZE) int pageSize,
			@RequestParam(required = false) String keyword) {
		if (keyword != null && !keyword.trim().isEmpty() && pageNumber > 0) {
			pageNumber = 0;
		}
		PaginationResponseDto<UserDto> userResponse = userService.getAllUsers(keyword, pageNumber, pageSize);
		return ResponseEntity.ok().body(userResponse);
	}
	
	@DeleteMapping("/auth/delete/{userId}")
    public ResponseEntity<String> delete(@PathVariable(value = "userId") Integer userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.ok("Posts deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete Posts: " + e.getMessage());
        }
    }

}
