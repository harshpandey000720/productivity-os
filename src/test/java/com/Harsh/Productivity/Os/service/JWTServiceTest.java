package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.entity.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class JWTServiceTest {
    private JWTService jwtService;
    private UserPrincipal userPrincipal;
    @BeforeEach
    void setUp() {
        jwtService = new JWTService("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY");
        User user = new User();
        user.setEmail("testjwt@gmail.com");
        user.setPassword("password");
        userPrincipal = new UserPrincipal(user);
    }
    @Test
    void generateAndValidateToken() {
        // Act
        String token = jwtService.generateToken("testjwt@gmail.com");
        // Assert
        assertNotNull(token);
        String username = jwtService.extractUserName(token);
        assertEquals("testjwt@gmail.com", username);
        assertTrue(jwtService.validateToken(token, userPrincipal));
    }
    @Test
    void validateToken_FailsForWrongUser() {
        String token = jwtService.generateToken("differentuser@gmail.com");
        assertFalse(jwtService.validateToken(token, userPrincipal));
    }
}