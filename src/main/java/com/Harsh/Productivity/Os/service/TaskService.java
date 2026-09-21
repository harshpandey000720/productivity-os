package com.Harsh.Productivity.Os.service;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.exception.ResourceNotFoundException;
import com.Harsh.Productivity.Os.exception.TaskAccessDeniedException;
import com.Harsh.Productivity.Os.exception.TaskAlreadyExistsException;
import com.Harsh.Productivity.Os.repository.TaskRepository;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class TaskService {
    public Task getTaskById(int id1) {
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new ResourceNotFoundException("User not found"));
        return user.getTasks().stream().filter(t->t.getId().equals(id1)).findFirst().orElseThrow(()->new ResourceNotFoundException("Task not found"));
    }
    public void deleteTask(int id1)  {
        Task task = taskRepository.findById(id1)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // 2. Fetch logged-in user
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated"));

        // 3. Verify ownership (Fixes your cross-user deletion bug!)
        if (!task.getUser().getId().equals(user.getId())) {
            throw new TaskAccessDeniedException("You do not have permission to delete this task");
        }

        // 4. Delete safely
        taskRepository.delete(task);
    }
    public Task updateTask(int id,Task task)  {
        Task existingTask=taskRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Task not found"));
        User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new ResourceNotFoundException("User not authenticated"));
        if (!existingTask.getUser().equals(user)) {
            throw new TaskAccessDeniedException("task belongs to another user");
        }
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setPriority(task.getPriority());
        existingTask.setDeadline(task.getDeadline());
        existingTask.setUpdatedAt(LocalDateTime.now());
        return  taskRepository.save(existingTask);
    }
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    public Task addTask(Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user=userRepository.findByEmail(email).orElseThrow();
        Optional<Task> existing=taskRepository.findByUserAndTitleAndDeadline(user,task.getTitle(),task.getDeadline());
        if(existing.isPresent()){
            throw new TaskAlreadyExistsException("Task already exists");
        }
        task.setUser(user);
        return taskRepository.save(task);
    }
    public List<Task> getTasks() {
        User user= userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new ResourceNotFoundException("User not found"));
        return user.getTasks();
    }
}