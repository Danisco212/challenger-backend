package com.danielisaac.challengerbackend.controllers.auth;

import com.danielisaac.challengerbackend.entities.User;
import com.danielisaac.challengerbackend.models.RegisterRequest;
import com.danielisaac.challengerbackend.repositories.UserRepository;
import com.danielisaac.challengerbackend.services.jwt.JwtUtil;
import com.danielisaac.challengerbackend.models.LoginRequest;
import com.danielisaac.challengerbackend.models.LoginResponse;
import com.danielisaac.challengerbackend.services.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthenticationController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    MyUserDetailService userDetailService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return loginFailed();
        }
        Optional<User> user = userRepository.findUserByUsername(loginRequest.getUsername());
        if (user.isPresent()) {
            final String jwt = jwtTokenUtil.generateToken(user.get());
            return ResponseEntity.ok(new LoginResponse(jwt));
        }
        return loginFailed();
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> createAccount(@RequestBody RegisterRequest registerRequest) {
        if(registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()){
            return fieldMissing("Email");
        } else if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()){
            return fieldMissing("Password");
        } else if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty()){
            return fieldMissing("Username");
        }
        Optional<User> mUser = userRepository.findUserByUsername(registerRequest.getUsername());
        if (mUser.isPresent()) {
            // username exists
            return fieldExists("Username");
        }
        mUser = userRepository.findUserByEmail(registerRequest.getEmail());
        if(mUser.isPresent()) {
            // email exists
            return fieldExists("Email");
        }
//        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        User user = new User(registerRequest);
        userRepository.save(user);
        return createAuthenticationToken(new LoginRequest(user.getUsername(), user.getPassword()));
    }

    private ResponseEntity<?> loginFailed() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Incorrect username of password");
        response.put("success", false);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> fieldExists(String field) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", field + " already exists");
        response.put("success", false);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> fieldMissing(String field) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", field + " is missing");
        response.put("success", false);
        return ResponseEntity.ok(response);
    }

}
