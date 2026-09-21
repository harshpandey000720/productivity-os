package com.Harsh.Productivity.Os.controller;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
public class TaskController {
    @Autowired
    TaskService service;
    @GetMapping("/Tasks")
    public List<Task> getTask(){
        return service.getTasks();
    }
    @GetMapping("/Tasks/{id1}")
    public Task getTaskById(@PathVariable int id1){
        return service.getTaskById(id1);
    }
    @PostMapping("/Tasks")
    public ResponseEntity<Task> addTask(@RequestBody @Valid Task task){
        Task savedTask = service.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }
    @PutMapping("/Tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable int id,@RequestBody Task task){
        Task updatedTask = service.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }
    @DeleteMapping("/Tasks/{id1}")
    public ResponseEntity<Task> deleteTask(@PathVariable int id1) {
        service.deleteTask(id1);
        return ResponseEntity.noContent().build();
//        Task task=service.deleteTask(id1);
//        return ResponseEntity.ok(task);
    }
}