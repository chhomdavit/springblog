package com.davit.springblog.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.davit.springblog.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer>{

	 	Page<Users> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
	 
	    Optional<Users> findByEmail(String email);

	    Optional<Users> findByName(String name);
}
