package com.Harsh.Productivity.Os.integration;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.TaskRepository;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Test
    void findByUserAndTitleAndDeadline() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("testrepo@gmail.com");
        user.setPassword("password");
        userRepository.save(user);
        LocalDate deadline = LocalDate.of(2026, 12, 31);
        Task task = new Task();
        task.setTitle("Repo Test Task");
        task.setDeadline(deadline);
        task.setUser(user);
        taskRepository.save(task);
        // Act
        Optional<Task> found = taskRepository.findByUserAndTitleAndDeadline(user, "Repo Test Task", deadline);
        Optional<Task> notFound = taskRepository.findByUserAndTitleAndDeadline(user, "Wrong Title", deadline);
        // Assert
        assertTrue(found.isPresent());
        assertEquals("Repo Test Task", found.get().getTitle());
        assertFalse(notFound.isPresent());
    }
}