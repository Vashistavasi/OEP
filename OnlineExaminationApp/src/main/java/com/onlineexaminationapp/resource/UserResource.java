package com.onlineexaminationapp.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlineexaminationapp.constant.SecurityConstant;
import com.onlineexaminationapp.customExceptions.ExceptionHandling;
import com.onlineexaminationapp.domain.User;
import com.onlineexaminationapp.domain.UserPrincipal;
import com.onlineexaminationapp.jwt.JWTTokenProvider;
import com.onlineexaminationapp.service.UserService;

@RestController
@RequestMapping(path = { "/", "/user"})
public class UserResource extends ExceptionHandling{

	   public static final String EMAIL_SENT = "An email with a new password was sent to: ";
	    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
	    private AuthenticationManager authenticationManager;
	    private UserService userService;
	    private JWTTokenProvider jwtTokenProvider;
	    @Autowired
	    public UserResource(AuthenticationManager authenticationManager, UserService userService, JWTTokenProvider jwtTokenProvider) {
	        this.authenticationManager = authenticationManager;
	        this.userService = userService;
	        this.jwtTokenProvider = jwtTokenProvider;
	    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.  OK);
    }
  
    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityConstant. JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(user));
        return headers;
    }
	 @PostMapping("/register")
	    public ResponseEntity<User> register(@RequestBody User user) throws Exception {
	        User newUser = userService.register(user.getFirstName(), 
	        		user.getLastName(), user.getUsername(), user.getEmail());
	        return new ResponseEntity<>(newUser,HttpStatus.OK);
	    }
}
