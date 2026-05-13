package com.minijira.task.controller;

import com.minijira.task.dto.*;
import com.minijira.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, getUserId(auth)));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getByProject(projectId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getById(taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long taskId,
                                                @Valid @RequestBody TaskRequest request,
                                                Authentication auth) {
        return ResponseEntity.ok(taskService.update(taskId, request, getUserId(auth)));
    }

    @PatchMapping("/{taskId}/move")
    public ResponseEntity<TaskResponse> move(@PathVariable Long taskId,
                                              @Valid @RequestBody TaskMoveRequest request) {
        return ResponseEntity.ok(taskService.move(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId) {
        taskService.delete(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long taskId,
                                                       @Valid @RequestBody CommentRequest request,
                                                       Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.addComment(taskId, request, getUserId(auth)));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getComments(taskId));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }
}
