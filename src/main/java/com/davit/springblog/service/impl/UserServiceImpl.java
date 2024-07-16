package com.davit.springblog.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.davit.springblog.dto.PaginationResponseDto;
import com.davit.springblog.dto.TokenRequestDto;
import com.davit.springblog.dto.UserDto;
import com.davit.springblog.dto.UserRequestDto;
import com.davit.springblog.dto.UserResponseDto;
import com.davit.springblog.entity.Roles;
import com.davit.springblog.entity.Users;
import com.davit.springblog.execption.AlreadyExistsException;
import com.davit.springblog.execption.ResourceNotFoundException;
import com.davit.springblog.jwt.JWTUtils;
import com.davit.springblog.repository.UserRepository;
import com.davit.springblog.service.FileUploadService;
import com.davit.springblog.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository usersRepository;
	private final PasswordEncoder passwordEncoder;
	private final JWTUtils jwtUtils;
	private final ModelMapper modelMapper;
	private final FileUploadService fileUploadService;
	private final AuthenticationManager authenticationManager;

	@Value("${project.upload}")
	private String path;

	@Override
	public UserResponseDto register(UserRequestDto userRequestDto) throws IOException {

		Optional<Users> existingUserByEmail = usersRepository.findByEmail(userRequestDto.getEmail());
		if (existingUserByEmail.isPresent()) {
			throw new AlreadyExistsException("Email already exists!");
		}

		Optional<Users> existingUserByName = usersRepository.findByName(userRequestDto.getName());
		if (existingUserByName.isPresent()) {
			throw new AlreadyExistsException("Name already exists!");
		}

		Users newUser = new Users();
		newUser.setName(userRequestDto.getName());
		newUser.setEmail(userRequestDto.getEmail());
		newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
		newUser.setRole(Roles.USER);
		newUser.setCreated(LocalDateTime.now());
		newUser.setUpdated(LocalDateTime.now());

		Users savedUser = usersRepository.save(newUser);

		var accessTolen = jwtUtils.generateToken(savedUser);
		var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), savedUser);

		UserResponseDto response = modelMapper.map(savedUser, UserResponseDto.class);
		response.setAccessToken(accessTolen);
		response.setRefreshToken(refreshToken);
		response.setUsersList(List.of(savedUser));

		return response;
	}

	@Override
	public UserResponseDto login(UserRequestDto userRequestDto) {

		UserResponseDto response = new UserResponseDto();
		try {
			var user = usersRepository.findByEmail(userRequestDto.getEmail());
			if (user.isPresent()) {
				authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(user.get().getEmail(), userRequestDto.getPassword()));
				user.get().setAttempt(0);
				user.get().setStatus("ACTIVE");
				usersRepository.save(user.get());

				var accessToken = jwtUtils.generateToken(user.get());
				var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user.get());

				response.setUsersList(List.of(user.get()));
				response.setAccessToken(accessToken);
				response.setRefreshToken(refreshToken);
			} else {
				throw new UsernameNotFoundException("User not found with email or name : " + userRequestDto.getEmail());
			}
		} catch (Exception e) {
			var user = usersRepository.findByEmail(userRequestDto.getEmail());
			user.ifPresent(u -> {
				u.setAttempt(u.getAttempt() + 1);
				if (u.getAttempt() >= 3) {
					u.setStatus("LOCKED");
				}
				usersRepository.save(u);
			});
		}
		return response;
	}

	@Override
	public UserResponseDto refreshToken(TokenRequestDto tokenRequestDto) {

		UserResponseDto response = new UserResponseDto();
		try {
			String ourEmail = jwtUtils.extractUsername(tokenRequestDto.getRefreshToken());
			Users users = usersRepository.findByEmail(ourEmail).orElseThrow();
			if (jwtUtils.isTokenValid(tokenRequestDto.getRefreshToken(), users)) {
				var jwt = jwtUtils.generateToken(users);
				response.setAccessToken(jwt);
				response.setRefreshToken(tokenRequestDto.getRefreshToken());
				response.setUsersList(List.of(users));
			}
			return response;

		} catch (Exception e) {
			return response;
		}
	}

	@Override
	public UserDto getMyProfile(String email) {
		UserDto userDto = null;
		try {
			Optional<Users> userOptional = usersRepository.findByEmail(email);
			if (userOptional.isPresent()) {
				Users user = userOptional.get();
				userDto = modelMapper.map(user, UserDto.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userDto;
	}

	@Override
	public PaginationResponseDto<UserDto> getAllUsers(String keyword, int pageNumber, int pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Users> userPages = keyword == null || keyword.trim().isEmpty() ? usersRepository.findAll(pageable)
				: usersRepository.findByNameContainingIgnoreCase(keyword, pageable);

		List<UserDto> userResponseList = userPages.getContent().stream().map(user -> {
			UserDto responseDto = modelMapper.map(user, UserDto.class);
			return responseDto;
		}).collect(Collectors.toList());

		PaginationResponseDto<UserDto> paginationResponse = new PaginationResponseDto<>();
		paginationResponse.setList(userResponseList);
		paginationResponse.setPageNumber(userPages.getNumber());
		paginationResponse.setPageSize(userPages.getSize());
		paginationResponse.setTotalElements(userPages.getTotalElements());
		paginationResponse.setTotalPages(userPages.getTotalPages());
		paginationResponse.setLast(userPages.isLast());

		return paginationResponse;
	}

	@Override
	public UserResponseDto update(Integer userId, UserRequestDto userRequestDto, MultipartFile file)
			throws IOException {
		Users existingUser = usersRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		String newSaveFile = null;
		try {
			if (file != null && !file.isEmpty()) {
				if (existingUser.getImageProfile() != null && !existingUser.getImageProfile().isEmpty()) {
					Files.deleteIfExists(Paths.get(path, existingUser.getImageProfile()));
				}
				newSaveFile = fileUploadService.saveFile(file, path);
			} else {
				newSaveFile = existingUser.getImageProfile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Error occurred while updating user profile image", e);
		}

		existingUser.setName(userRequestDto.getName());
		existingUser.setEmail(userRequestDto.getEmail());
		if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
			String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());
			existingUser.setPassword(encodedPassword);
		}
		existingUser.setTel(userRequestDto.getTel());
		if (userRequestDto.getRole() != null && !userRequestDto.getRole().isEmpty()) {
			existingUser.setRole(Roles.valueOf(userRequestDto.getRole()));
		} else {
			existingUser.setRole(existingUser.getRole());
		}
		existingUser.setCreated(LocalDateTime.now());
		existingUser.setUpdated(LocalDateTime.now());
		existingUser.setImageProfile(newSaveFile);
		if (existingUser.getAttempt() == 0) {
			existingUser.setStatus("ACTIVE");
		}

		Users savedUser = usersRepository.save(existingUser);

		UserResponseDto userResponseDto = new UserResponseDto();
		userResponseDto.setUsersList(List.of(savedUser));
		return userResponseDto;
	}

	
	@Override
	public void delete(Integer userId) throws IOException {
		 Users existingUsers = usersRepository.findById(userId)
	                .orElseThrow(() -> new ResourceNotFoundException("Posts not found with id: " + userId));

	        String uploadDirectory = path;
	        String photoFileName = existingUsers.getImageProfile();

	        if (photoFileName != null && !photoFileName.isEmpty()) {
	            try {
	                Files.deleteIfExists(Paths.get(uploadDirectory, photoFileName));
	            } catch (IOException e) {
	                e.printStackTrace();
	                throw new IOException("Failed to delete photo", e);
	            }
	        }
	        usersRepository.deleteById(userId);
	}

}
