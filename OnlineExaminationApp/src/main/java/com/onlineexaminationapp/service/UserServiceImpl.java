package com.onlineexaminationapp.service;



import static com.onlineexaminationapp.constant.enumeration.Role.ROLE_USER;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.onlineexaminationapp.constant.FileConstant;
import com.onlineexaminationapp.constant.UserImplConstant;
import com.onlineexaminationapp.customExceptions.EmailExistException;
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
	        return RandomStringUtils.randomAlphanumeric(10);
	    }
	    
	    private String encodePassword(String password) {
	        return passwordEncoder.encode(password);
	    }
	    private String getTemporaryProfileImageUrl(String username) {
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
}
