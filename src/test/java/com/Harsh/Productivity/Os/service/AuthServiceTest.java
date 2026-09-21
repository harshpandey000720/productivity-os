package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.dto.AuthResponse;
import com.Harsh.Productivity.Os.dto.LoginRequest;
import com.Harsh.Productivity.Os.dto.RegisterRequest;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepository userRepository;
    @Mock
    JWTService jwtService;
    @InjectMocks
    private AuthService authService;
    @Test
    public void alreadyExists() {
        RegisterRequest registerRequest=new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        User existingUser=new User();
        existingUser.setEmail("test@gmail.com");
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(existingUser));
        AuthResponse response=authService.register(registerRequest);
        assertEquals("Email already exists",response.getMessage());
        verify(userRepository,never()).save(any(User.class));
    }
    @Test
    public void registerUser(){
        RegisterRequest registerRequest=new RegisterRequest();
        registerRequest.setEmail("test@gmail.com");
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        AuthResponse response=authService.register(registerRequest);
        assertEquals("Registration Successful",response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    public void loginFail(){
        LoginRequest request=new LoginRequest();
        Authentication authentication=mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        AuthResponse response=authService.login(request);
        assertEquals("fail",response.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
    @Test
    public void loginThrowsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> authService.login(request));
    }
    @Test
    public void login(){
        LoginRequest loginRequest=new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("1234");
        Authentication authentication=mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("test@gmail.com")).thenReturn("mocked-jwt-token");
        AuthResponse response=authService.login(loginRequest);
        assertEquals("mocked-jwt-token",response.getMessage());
        verify(authenticationManager,times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService,times(1)).generateToken("test@gmail.com");
    }
}