package com.Harsh.Productivity.Os.controller;

import com.Harsh.Productivity.Os.dto.AuthResponse;
import com.Harsh.Productivity.Os.dto.LoginRequest;
import com.Harsh.Productivity.Os.dto.RegisterRequest;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    @Autowired
    AuthService authService;
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request){
        System.out.println("HIT REGISTER ENDPOINT!");
        System.out.println("Received email: " + request.getEmail());
        return authService.register(request);
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }

}
