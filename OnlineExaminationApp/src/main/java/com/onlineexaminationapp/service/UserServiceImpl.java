package com.onlineexaminationapp.service;



import static com.onlineexaminationapp.constant.FileConstant.DIRECTORY_CREATED;
import static com.onlineexaminationapp.constant.FileConstant.DOT;
import static com.onlineexaminationapp.constant.FileConstant.FILE_SAVED_IN_FILE_SYSTEM;
import static com.onlineexaminationapp.constant.FileConstant.FORWARD_SLASH;
import static com.onlineexaminationapp.constant.FileConstant.JPG_EXTENSION;
import static com.onlineexaminationapp.constant.FileConstant.NOT_AN_IMAGE_FILE;
import static com.onlineexaminationapp.constant.FileConstant.USER_FOLDER;
import static com.onlineexaminationapp.constant.FileConstant.USER_IMAGE_PATH;
import static com.onlineexaminationapp.constant.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.onlineexaminationapp.constant.EmailConstant;
import com.onlineexaminationapp.constant.FileConstant;
import com.onlineexaminationapp.constant.UserImplConstant;
import com.onlineexaminationapp.constant.enumeration.Role;
import com.onlineexaminationapp.customExceptions.EmailExistException;
import com.onlineexaminationapp.customExceptions.EmailNotFoundException;
import com.onlineexaminationapp.customExceptions.NotAnImageFileException;
import com.onlineexaminationapp.customExceptions.UserNotFoundException;
import com.onlineexaminationapp.customExceptions.UsernameExistException;
import com.onlineexaminationapp.domain.User;
import com.onlineexaminationapp.domain.UserPrincipal;
import com.onlineexaminationapp.repository.UserRepository;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService ,UserDetailsService{

	
	  private Logger LOGGER = LoggerFactory.getLogger(getClass());
	  @Autowired
	    private UserRepository userRepository;
	  @Autowired  
	  private BCryptPasswordEncoder passwordEncoder;
	  //  private LoginAttemptService loginAttemptService;
	 //   private EmailService emailService;
//	    @Autowired
//	    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder
//	    		/*, LoginAttemptService loginAttemptService, EmailService emailService*/) {
//	        this.userRepository = userRepository;
//	        this.passwordEncoder = passwordEncoder;
//	       // this.loginAttemptService = loginAttemptService;
//	       // this.emailService = emailService;
//	    }
	
	   @Override
	public User register(String firstName, String lastName, String username, String email)
			throws UserNotFoundException, UsernameExistException, EmailExistException {
	        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
	        User user = new User();
	        user.setUserId(generateUserId());
	        String password = generatePassword();
	        user.setFirstName(firstName);
	        user.setLastName(lastName);
	        user.setUsername(username);
	        user.setEmail(email);
	        user.setJoinDate(new Date());
	        user.setPassword(encodePassword(password));
	        user.setActive(true);
	        user.setNotLocked(true);
	        user.setRole( ROLE_USER.name());
	        user.setAuthorities(ROLE_USER.getAuthorities());
	        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
	        userRepository.save(user);
	        System.err.println("New user password: " + password);
	        LOGGER.info("New user password: " + password);
	        //emailService.sendNewPasswordEmail(firstName, password, email);
	        return user;
	    }
	 

	   private String generateUserId() {
	        return RandomStringUtils.randomNumeric(10);
	    }
	    private String generatePassword() {
	    	return "password";
	       // return RandomStringUtils.randomAlphanumeric(10);
	    }
	    
	    private String encodePassword(String password) {
	        return passwordEncoder.encode(password);
	    }
	    private String getTemporaryProfileImageUrl(String username) {
	    	//return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.  DEFAULT_USER_IMAGE_PATH ).toUriString();
	    	System.out.print(ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.  DEFAULT_USER_IMAGE_PATH + username).toUriString());
	        return ServletUriComponentsBuilder.fromCurrentContextPath().path(FileConstant.  DEFAULT_USER_IMAGE_PATH + username).toUriString();
	    }


	    @Override
	    public User findUserByUsername(String username) {
	        return userRepository.findUserByUsername(username);
	    }
	    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
	        User userByNewUsername = findUserByUsername(newUsername);
	        User userByNewEmail = findUserByEmail(newEmail);
	        if(StringUtils.isNotBlank(currentUsername)) {
	            User currentUser = findUserByUsername(currentUsername);
	            if(currentUser == null) {
	                throw new UserNotFoundException( UserImplConstant. NO_USER_FOUND_BY_USERNAME + currentUsername);
	            }
	            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
	                throw new UsernameExistException( UserImplConstant.USERNAME_ALREADY_EXISTS);
	            }
	            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
	                throw new EmailExistException( UserImplConstant.EMAIL_ALREADY_EXISTS);
	            }
	            return currentUser;
	        } else {
	            if(userByNewUsername != null) {
	                throw new UsernameExistException( UserImplConstant.USERNAME_ALREADY_EXISTS);
	            }
	            if(userByNewEmail != null) {
	                throw new EmailExistException( UserImplConstant.EMAIL_ALREADY_EXISTS);
	            }
	            return null;
	        }
	
	
	
}

	    @Override
	    public User findUserByEmail(String email) {
	        return userRepository.findUserByEmail(email);
	    }

	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        User user = userRepository.findUserByUsername(username);
	        if (user == null) {
	            LOGGER.error(UserImplConstant. NO_USER_FOUND_BY_USERNAME + username);
	            throw new UsernameNotFoundException(UserImplConstant.NO_USER_FOUND_BY_USERNAME + username);
	        } else {
	           // validateLoginAttempt(user);
	            user.setLastLoginDateDisplay(user.getLastLoginDate());
	            user.setLastLoginDate(new Date());
	            userRepository.save(user);
	            UserPrincipal userPrincipal = new UserPrincipal(user);
	            LOGGER.info(UserImplConstant.FOUND_USER_BY_USERNAME + username);
	            return userPrincipal;
	        }
	    }
//	    private void validateLoginAttempt(User user) {
//	        if(user.isNotLocked()) {
//	            if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
//	                user.setNotLocked(false);
//	            } else {
//	                user.setNotLocked(true);
//	            }
//	        } else {
//	            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
//	        }
//	    }
	    
	    
	    
	    
	    
	    
	    
	    @Override
	    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
	        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
	        User user = new User();
	        String password = generatePassword();
	        user.setUserId(generateUserId());
	        user.setFirstName(firstName);
	        user.setLastName(lastName);
	        user.setJoinDate(new Date());
	        user.setUsername(username);
	        user.setEmail(email);
	        user.setPassword(encodePassword(password));
	        user.setActive(isActive);
	        user.setNotLocked(isNonLocked);
	        user.setRole(getRoleEnumName(role).name());
	        user.setAuthorities(getRoleEnumName(role).getAuthorities());
	        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
	        userRepository.save(user);
	        saveProfileImage(user, profileImage);
	        LOGGER.info("New user password: " + password);
	        return user;
	    }
	    private Role getRoleEnumName(String role) {
	        return Role.valueOf(role.toUpperCase());
	    }
	    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
	        if (profileImage != null) {
	            if(!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
	                throw new NotAnImageFileException(profileImage.getOriginalFilename() + NOT_AN_IMAGE_FILE);
	            }
	            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
	            if(!Files.exists(userFolder)) {
	                Files.createDirectories(userFolder);
	                LOGGER.info(DIRECTORY_CREATED + userFolder);
	            }
	            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
	            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
	            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
	            userRepository.save(user);
	            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
	        }
	    }
	    
	    
	    private String setProfileImageUrl(String username) {
	        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH
	        + username + DOT + JPG_EXTENSION).toUriString();
	    }
	    
	    
	    
	    @Override
	    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
	        User currentUser = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);
	        currentUser.setFirstName(newFirstName);
	        currentUser.setLastName(newLastName);
	        currentUser.setUsername(newUsername);
	        currentUser.setEmail(newEmail);
	        currentUser.setActive(isActive);
	        currentUser.setNotLocked(isNonLocked);
	        currentUser.setRole(getRoleEnumName(role).name());
	        currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
	        userRepository.save(currentUser);
	        saveProfileImage(currentUser, profileImage);
	        return currentUser;
	    }
	    
	    @Override
	    public List<User> getUsers() {
	        return (List<User>) userRepository.findAll();
	    }


	    @Override
	    public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
	        User user = userRepository.findUserByEmail(email);
	        if (user == null) {
	            throw new EmailNotFoundException(UserImplConstant . NO_USER_FOUND_BY_EMAIL + email);
	        }
	        String password = generatePassword();
	        user.setPassword(encodePassword(password));
	        userRepository.save(user);
	        LOGGER.info("New user password: " + password);
	       // emailService.sendNewPasswordEmail(user.getFirstName(), password, user.getEmail());
	    }


		  @Override
		    public void deleteUser(String username) throws IOException {
		        User user = userRepository.findUserByUsername(username);
		        Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
		        FileUtils.deleteDirectory(new File(userFolder.toString()));
		        userRepository.deleteById(user.getId());
		    }
		  
		  @Override
		    public User updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotAnImageFileException {
		        User user = validateNewUsernameAndEmail(username, null, null);
		        saveProfileImage(user, profileImage);
		        return user;
		    }
}
