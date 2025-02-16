package com.example.helpdesk.controller;

import com.example.helpdesk.model.User;
import com.example.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        Optional<User> user = userRepository.findByEmail(loginUser.getEmail());

        if (user.isPresent() && user.get().getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.ok("Login successful!");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials!");
        }
    }
}
