package com.onlineexaminationapp.service;

import org.springframework.stereotype.Service;

import com.onlineexaminationapp.domain.User;

@Service
public interface UserService {

	User register(String firstName, String lastName, String username, String email) throws Exception;
    User findUserByUsername(String username);
	User findUserByEmail(String email);
}