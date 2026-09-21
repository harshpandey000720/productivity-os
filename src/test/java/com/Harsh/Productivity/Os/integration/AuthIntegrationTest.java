package com.Harsh.Productivity.Os.integration;
import com.Harsh.Productivity.Os.dto.AuthResponse;
import com.Harsh.Productivity.Os.dto.LoginRequest;
import com.Harsh.Productivity.Os.dto.RegisterRequest;
import com.Harsh.Productivity.Os.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Test
    void registerUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("integration@gmail.com");
        registerRequest.setPassword("password");
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        ).andExpect(status().isOk());
        assertTrue(
                userRepository.findByEmail("integration@gmail.com").isPresent()
        );
    }
    @Test
    void registerDuplicateEmail() throws Exception {
        RegisterRequest firstRequest = new RegisterRequest();
        firstRequest.setName("First User");
        firstRequest.setEmail("duplicate@gmail.com");
        firstRequest.setPassword("password");
        mockMvc.perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(firstRequest))
        ).andExpect(status().isOk());
        RegisterRequest secondRequest = new RegisterRequest();
        secondRequest.setName("Second User");
        secondRequest.setEmail("duplicate@gmail.com");
        secondRequest.setPassword("password");
        mockMvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest))
        )
        .andExpect(status().isOk())
        .andExpect(content().json("""
                {
                    "message":"Email already exists"
                }
        """));
    }
    @Test
    void authenticatedUserCanAccessTasks() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Login User");
        registerRequest.setEmail("login@gmail.com");
        registerRequest.setPassword("password");
        mockMvc.perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
        ).andExpect(status().isOk());
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@gmail.com");
        loginRequest.setPassword("password");
        MvcResult loginResult=mockMvc.perform(
                post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpect(status().isOk())
         .andExpect(jsonPath("$.message").isNotEmpty())
         .andReturn();
        AuthResponse authResponse =
                objectMapper.readValue(
                        loginResult.getResponse().getContentAsString(),
                        AuthResponse.class
                );
        String token = authResponse.getMessage();
        mockMvc.perform(
                get("/Tasks")
                        .header("Authorization","Bearer "+token)
        )
                .andExpect(status().isOk());
    }

}
