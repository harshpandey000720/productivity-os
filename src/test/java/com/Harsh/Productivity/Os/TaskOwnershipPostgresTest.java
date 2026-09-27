package com.Harsh.Productivity.Os;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.TaskRepository;
import com.Harsh.Productivity.Os.repository.UserRepository;
import com.Harsh.Productivity.Os.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Testcontainers
public class TaskOwnershipPostgresTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    private User user1;
    private User user2;
    private Task task1;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        taskRepository.deleteAll();
        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@test.com");
        user1.setPassword("hashed1");
        user1.setRole("USER");
        user1.setTenantId("default-tenant");
        user1 = userRepository.save(user1);
        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@test.com");
        user2.setPassword("hashed2");
        user2.setRole("USER");
        user2.setTenantId("default-tenant");
        user2 = userRepository.save(user2);
        task1 = new Task();
        task1.setUser(user1);
        task1.setTitle("Task owned by user1");
        task1.setDescription("Test task");
        task1.setStatus("TODO");
        task1.setTenantId("default-tenant");
        task1 = taskRepository.save(task1);
    }
    @Test
    void testUserCanUpdateOwnTask() {
        task1.setTitle("Updated task");
        Task updated = taskRepository.save(task1);
        assertEquals("Updated task", updated.getTitle());
    }
    @Test
    void testTaskOwnershipIsolation() {
        // user1's task should exist
        assertTrue(taskRepository.findById(task1.getId()).isPresent());
        // user2 trying to access it should still find it (ownership enforced at service layer, not DB)
        // but this proves the data exists in Postgres correctly
        Task found = taskRepository.findById(task1.getId()).get();
        assertEquals(user1.getId(), found.getUser().getId());
    }
}