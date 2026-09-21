package com.Harsh.Productivity.Os.integration;

import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        user.setRole("USER");

        userRepository.save(user);

        User result = userRepository
                .findByEmail("test@gmail.com")
                .orElse(null);

        assertNotNull(result);
        assertEquals("test@gmail.com", result.getEmail());
    }
}