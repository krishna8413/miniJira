package com.minijira.task.repository;

import com.minijira.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdOrderByColumnIdAscPositionAsc(Long projectId);
    List<Task> findByColumnIdOrderByPositionAsc(Long columnId);
    int countByColumnId(Long columnId);
}
