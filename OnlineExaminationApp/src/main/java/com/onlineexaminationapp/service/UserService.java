package com.onlineexaminationapp.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onlineexaminationapp.customExceptions.EmailExistException;
import com.onlineexaminationapp.customExceptions.EmailNotFoundException;
import com.onlineexaminationapp.customExceptions.NotAnImageFileException;
import com.onlineexaminationapp.customExceptions.UserNotFoundException;
import com.onlineexaminationapp.customExceptions.UsernameExistException;
import com.onlineexaminationapp.domain.User;

@Service
public interface UserService {

	User register(String firstName, String lastName, String username, String email) throws Exception;
    User findUserByUsername(String username);
	User findUserByEmail(String email);
	User addNewUser(String firstName, String lastName, String username, String email, String role, boolean parseBoolean,
			boolean parseBoolean2, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;
	User updateUser(String currentUsername, String firstName, String lastName, String username, String email,
			String role, boolean parseBoolean, boolean parseBoolean2, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;
	List<User> getUsers();
	void resetPassword(String email) throws MessagingException, EmailNotFoundException;
	void deleteUser(String username) throws IOException;
	User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException;
}