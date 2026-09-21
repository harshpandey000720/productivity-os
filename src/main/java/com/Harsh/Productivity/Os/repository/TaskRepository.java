package com.Harsh.Productivity.Os.repository;
import com.Harsh.Productivity.Os.entity.Task;
import com.Harsh.Productivity.Os.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;
@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {
    Optional<Task> findByUserAndTitleAndDeadline(User user, String title, LocalDate deadline);
}
