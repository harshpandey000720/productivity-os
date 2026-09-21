package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.exception.ResourceNotFoundException;
import com.Harsh.Productivity.Os.exception.TaskAccessDeniedException;
import com.Harsh.Productivity.Os.exception.TaskAlreadyExistsException;
import com.Harsh.Productivity.Os.repository.TaskRepository;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    TaskRepository taskRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;
    @InjectMocks
    TaskService taskService;
    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }
    @Test
    void getTaskByIdFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        Task task=new Task();
        task.setId(1);
        User user=new User();
//        user.setEmail("test@gmail.com");
        user.setTasks(List.of(task));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        Task result=taskService.getTaskById(1);
        assertEquals(task,result);
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
    }
    @AfterEach
    void clearSecurityContext(){
        SecurityContextHolder.clearContext();
    }
    @Test
    void getTaskByIdNotFound(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        User user=new User();
        user.setEmail("test@gmail.com");
        user.setTasks(List.of());
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->taskService.getTaskById(1));
        assertEquals("Task not found",exception.getMessage());
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
    }
    @Test
    void getTaskByIdNotFound2(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->taskService.getTaskById(1));
        assertEquals("User not found",exception.getMessage());
    }
    @Test
    void getTasksButUserNotFound(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        ResourceNotFoundException exception=assertThrows(ResourceNotFoundException.class,()->taskService.getTasks());
        assertEquals("User not found",exception.getMessage());
    }
    @Test
    void getTasksUserFound(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        Task task=new Task();
        User user=new User();
        user.setEmail("test@gmail.com");
        user.setTasks(List.of(task));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        List<Task> result=taskService.getTasks();
        assertEquals(user.getTasks(), result);
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
    }
    @Test
    void addTask(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        Task task=new Task();
        User user=new User();
        user.setEmail("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByUserAndTitleAndDeadline(user,task.getTitle(),task.getDeadline())).thenReturn(Optional.empty());
        when(taskRepository.save(task)).thenReturn(task);
        Task result=taskService.addTask(task);
        assertEquals(task,result);
        assertEquals(user,task.getUser());
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
        verify(taskRepository,times(1)).findByUserAndTitleAndDeadline(user,task.getTitle(),task.getDeadline());
        verify(taskRepository,times(1)).save(task);
    }
    @Test
    void addTaskFailsDueToDuplicate(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        Task task=new Task();
        User user=new User();
        user.setEmail("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.findByUserAndTitleAndDeadline(user,task.getTitle(),task.getDeadline())).thenReturn(Optional.of(task));
        TaskAlreadyExistsException exception=assertThrows(TaskAlreadyExistsException.class,()->taskService.addTask(task));
        assertEquals("Task already exists",exception.getMessage());
    }
    @Test
    void addTaskUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        Task task = new Task();
        assertThrows(java.util.NoSuchElementException.class, () -> taskService.addTask(task));
    }
    @Test
    void updateTask(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        User user=new User();
        Task existingTask=new Task();
        existingTask.setId(1);
        existingTask.setUser(user);
        Task taskUpdates=new Task();
        taskUpdates.setTitle("Updated Title");
        taskUpdates.setDescription("Updated Description");
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);
        Task result=taskService.updateTask(1,taskUpdates);
        assertEquals("Updated Title",result.getTitle());
        assertEquals("Updated Description",result.getDescription());
        verify(taskRepository,times(1)).findById(1);
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
        verify(taskRepository,times(1)).save(existingTask);
    }
    @Test
    void updateTaskFailsDueToUnauthorized(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("unknown@example.com");
        User owner = new User();
        owner.setId(1);
        owner.setEmail("owner@example.com");
        User loggedInUser = new User();
        loggedInUser.setId(2);
        loggedInUser.setEmail("unknown@example.com");
        Task existingTask = new Task();
        existingTask.setId(1);
        existingTask.setUser(owner);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.of(loggedInUser));
        TaskAccessDeniedException exception = assertThrows(TaskAccessDeniedException.class, () -> taskService.updateTask(1, new Task()));
        assertEquals("task belongs to another user",exception.getMessage());
    }
    @Test
    void updateTaskUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");
        Task existingTask = new Task();
        existingTask.setId(1);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1,new Task()));
        assertEquals("User not authenticated",exception.getMessage());
        verify(taskRepository,times(1)).findById(1);
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
        verify(taskRepository,never()).save(any());
    }
    @Test
    void updateTaskFailsDueToTaskNotFound(){
        when(taskRepository.findById(1)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1,new Task()));
        assertEquals("Task not found",exception.getMessage());
        verify(taskRepository,times(1)).findById(1);
        verifyNoMoreInteractions(taskRepository);
    }
    @Test
    void deleteTask(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");

        Task task=new Task();
        task.setId(1);
        User user=new User();
        user.setId(1); // Ensure IDs match
        task.setUser(user);
        user.setTasks(List.of(task));
        user.setEmail("test@gmail.com");

        // ADD THIS LINE: Mock the findById call
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));

        taskService.deleteTask(1);

        verify(taskRepository,times(1)).delete(task);
        // Changed times(2) to times(1) because getTaskById is no longer called
        verify(userRepository,times(1)).findByEmail("test@gmail.com");
    }

    @Test
    void deleteTaskFailsDueToAccessDenied(){
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@gmail.com");

        User owner = new User();
        owner.setId(1);
        User loggedInUser = new User();
        loggedInUser.setId(2);

        Task task = new Task();
        task.setId(1);
        task.setUser(owner);
        loggedInUser.setTasks(List.of(task));

        // ADD THIS LINE: Mock the findById call
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(loggedInUser));

        TaskAccessDeniedException exception = assertThrows(TaskAccessDeniedException.class, () -> taskService.deleteTask(1));
        assertEquals("You do not have permission to delete this task",exception.getMessage());
        verify(taskRepository,never()).delete(any());
    }
}