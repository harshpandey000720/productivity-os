package com.Harsh.Productivity.Os.integration;
import com.Harsh.Productivity.Os.dto.AuthResponse;
import com.Harsh.Productivity.Os.dto.LoginRequest;
import com.Harsh.Productivity.Os.dto.RegisterRequest;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.TaskRepository;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.annotation.DirtiesContext;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;
    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Test
    void createTask() throws Exception {
        String token = getToken("taskuser@gmail.com", "password");
        Task task = new Task();
        task.setTitle("Integration Test Task");
        task.setDescription("Testing POST /Tasks");
        task.setStatus("TODO");
        task.setPriority("HIGH");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated());
        assertEquals(1, taskRepository.count());
        Task savedTask = taskRepository.findAll().get(0);
        assertEquals("Integration Test Task", savedTask.getTitle());
        assertEquals("TODO", savedTask.getStatus());
        assertEquals("HIGH", savedTask.getPriority());
        assertNotNull(savedTask.getUser());
        User savedUser = userRepository
                .findByEmail("taskuser@gmail.com")
                .orElseThrow();
        assertEquals(savedUser.getId(), savedTask.getUser().getId());
    }
    @Test
    void duplicateTask() throws Exception {
        String token = getToken("dupliacteuser@gmail.com", "password");
        //Create first task
        Task task = new Task();
        task.setTitle("Duplicate Task");
        task.setDescription("Testing duplicate task");
        task.setStatus("TODO");
        task.setPriority("HIGH");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated());
        mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isConflict())
                .andExpect(content().string("Task already exists"));
        assertEquals(1, taskRepository.count());
    }
    private String getToken(String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Authenticated User");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        mockMvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
        ).andExpect(status().isOk());
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(
                        post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse authResponse =
                objectMapper.readValue(
                        loginResult.getResponse().getContentAsString(),
                        AuthResponse.class
                );
        String token = authResponse.getMessage();
        return token;
    }
    @Test
    void getTasks() throws Exception {
        String token = getToken("gettaskuser@gmail.com", "password");
        Task task = new Task();
        task.setTitle("GET Task");
        task.setDescription("Testing GET/Tasks");
        task.setStatus("TODO");
        task.setPriority("MEDIUM");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated());
        mockMvc.perform(
                        get("/Tasks")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("GET Task"))
                .andExpect(jsonPath("$[0].description").value("Testing GET/Tasks"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"));
    }
    @Test
    void getTaskById() throws Exception {
        String token = getToken("gettaskuser@gmail.com", "password");
        Task task = new Task();
        task.setTitle("GET Task");
        task.setDescription("Testing GET/Tasks");
        task.setStatus("TODO");
        task.setPriority("MEDIUM");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        MvcResult result = mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Task savedTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Task.class
        );

        int taskId = savedTask.getId();
        mockMvc.perform(
                        get("/Tasks/" + taskId)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("GET Task"))
                .andExpect(jsonPath("$.description").value("Testing GET/Tasks"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }
    @Test
    void getTaskByIdNotFound() throws Exception {
        String token = getToken("notfound@gmail.com", "password");
        mockMvc.perform(
                        get("/Tasks/999")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }
    @Test
    void updateTask() throws Exception {
        String token = getToken("updateuser@gmail.com", "password");
        Task task = new Task();
        task.setTitle("Old Title");
        task.setDescription("Old Description");
        task.setStatus("TODO");
        task.setPriority("LOW");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        MvcResult result = mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated())
                .andReturn();
        Task savedTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Task.class
        );
        int taskId = savedTask.getId();
        Task updatedTask = new Task();
        updatedTask.setTitle("New Title");
        updatedTask.setDescription("New Description");
        updatedTask.setStatus("DONE");
        updatedTask.setPriority("HIGH");
        updatedTask.setDeadline(LocalDate.of(2030, 9, 1));
        mockMvc.perform(
                        put("/Tasks/" + taskId)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }
    @Test
    void updateTaskNotFound() throws Exception {
        String token = getToken("tasknotfound@gmail.com", "password");
        Task updatedTask = new Task();
        updatedTask.setTitle("Non-existent Task");
        updatedTask.setDescription("Attempting to update missing task");
        updatedTask.setStatus("DONE");
        updatedTask.setPriority("HIGH");
        updatedTask.setDeadline(LocalDate.of(2030, 9, 1));
        mockMvc.perform(
                        put("/Tasks/999")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedTask))
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }
    @Test
    void updateTaskBelongingToAnotherUser() throws Exception {
        String userAToken=getToken("usera_update@gmail.com","password");
        Task task=new Task();
        task.setTitle("User A Task");
        task.setDescription("Belongs to A");
        task.setStatus("TODO");
        task.setPriority("LOW");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        MvcResult result=mockMvc.perform(
                post("/Tasks")
                        .header("Authorization","Bearer "+userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task))
        )
                .andExpect(status().isCreated())
                .andReturn();
        Task savedTask=objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Task.class
        );
        int taskId = savedTask.getId();
        String userBToken=getToken("userb_update@gmail.com","password");
        Task updatePayload=new Task();
        updatePayload.setTitle("Malicious Update");
        updatePayload.setDescription("Hacking task");
        updatePayload.setStatus("DONE");
        updatePayload.setPriority("HIGH");
        updatePayload.setDeadline(LocalDate.of(2030, 1, 1));
        mockMvc.perform(
                put("/Tasks/" + taskId)
                .header("Authorization", "Bearer "+userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        )
                .andExpect(status().isForbidden());
        Task unalteredTask=taskRepository.findById(taskId).orElseThrow();
        assertEquals("User A Task",unalteredTask.getTitle());
    }
    @Test
    void deleteTaskSuccess() throws Exception {
        String token=getToken("delete_user@gmail.com","password");
        Task task = new Task();
        task.setTitle("To be deleted");
        task.setDescription("Delete test");
        task.setStatus("TODO");
        task.setPriority("LOW");
        task.setDeadline(LocalDate.of(2029, 9, 1));
        MvcResult result=mockMvc.perform(
                post("/Tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task))
        )
                .andExpect(status().isCreated())
                .andReturn();
        Task savedTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Task.class
        );
        int taskId = savedTask.getId();
        mockMvc.perform(
                delete("/Tasks/" + taskId)
                .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isNoContent());
        assertEquals(0,taskRepository.count());
    }
    @Test
    void deleteTaskNotFound() throws Exception {
        String token=getToken("delete_notfound@gmail.com","password");
        mockMvc.perform(
                delete("/Tasks/999")
                .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Task not found"));
    }
    @Test
    void deleteTaskBelongingToAnotherUser() throws Exception {
        String userAToken=getToken("usera_del@gmail.com","password");
        Task task = new Task();
        task.setTitle("User A Task to Delete");
        task.setDescription("Safe from B");
        task.setStatus("TODO");
        task.setPriority("LOW");
        task.setDeadline(LocalDate.of(2029, 9, 1));

        MvcResult result = mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + userAToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(task))
                )
                .andExpect(status().isCreated())
                .andReturn();

        Task savedTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Task.class
        );
        int taskId = savedTask.getId();
        String userBToken = getToken("userb_del@gmail.com", "password");

        mockMvc.perform(
                delete("/Tasks/" + taskId)
                        .header("Authorization", "Bearer " + userBToken)
        )
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to delete this task"));
        assertEquals(1, taskRepository.count());
    }
    @Test
    void createTask_FailsValidation() throws Exception {
        String token = getToken("validationuser@gmail.com", "password");

        Task invalidTask = new Task();
        // Missing the Title, which is annotated with @NotBlank
        invalidTask.setDescription("Missing title task");

        mockMvc.perform(
                        post("/Tasks")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidTask))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input data: Please check your fields."));
    }
}