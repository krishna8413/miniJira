package com.minijira.task.service;

import com.minijira.task.dto.*;
import com.minijira.task.entity.Task;
import com.minijira.task.entity.TaskComment;
import com.minijira.task.repository.TaskCommentRepository;
import com.minijira.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TaskResponse create(TaskRequest request, Long userId) {
        int position = taskRepository.countByColumnId(request.getColumnId());

        Task task = Task.builder()
                .projectId(request.getProjectId())
                .columnId(request.getColumnId())
                .title(request.getTitle())
                .description(request.getDescription())
                .assigneeId(request.getAssigneeId())
                .createdBy(userId)
                .priority(Task.Priority.valueOf(request.getPriority().toUpperCase()))
                .dueDate(request.getDueDate())
                .position(position)
                .build();

        taskRepository.save(task);
        TaskResponse response = toResponse(task);
        messagingTemplate.convertAndSend("/topic/project/" + task.getProjectId(), Map.of("event", "TASK_CREATED", "task", response));
        return response;
    }

    public List<TaskResponse> getByProject(Long projectId) {
        return taskRepository.findByProjectIdOrderByColumnIdAscPositionAsc(projectId)
                .stream().map(this::toResponse).toList();
    }

    public TaskResponse getById(Long taskId) {
        return toResponse(getTaskOrThrow(taskId));
    }

    @Transactional
    public TaskResponse update(Long taskId, TaskRequest request, Long userId) {
        Task task = getTaskOrThrow(taskId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeId(request.getAssigneeId());
        task.setPriority(Task.Priority.valueOf(request.getPriority().toUpperCase()));
        task.setDueDate(request.getDueDate());
        taskRepository.save(task);
        TaskResponse response = toResponse(task);
        messagingTemplate.convertAndSend("/topic/project/" + task.getProjectId(), Map.of("event", "TASK_UPDATED", "task", response));
        return response;
    }

    @Transactional
    public TaskResponse move(Long taskId, TaskMoveRequest request) {
        Task task = getTaskOrThrow(taskId);
        task.setColumnId(request.getTargetColumnId());
        task.setPosition(request.getNewPosition());
        taskRepository.save(task);
        TaskResponse response = toResponse(task);
        messagingTemplate.convertAndSend("/topic/project/" + task.getProjectId(), Map.of("event", "TASK_MOVED", "task", response));
        return response;
    }

    @Transactional
    public void delete(Long taskId) {
        Task task = getTaskOrThrow(taskId);
        taskRepository.delete(task);
        messagingTemplate.convertAndSend("/topic/project/" + task.getProjectId(), Map.of("event", "TASK_DELETED", "taskId", taskId));
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, Long userId) {
        Task task = getTaskOrThrow(taskId);
        TaskComment comment = TaskComment.builder()
                .task(task)
                .userId(userId)
                .content(request.getContent())
                .build();
        commentRepository.save(comment);
        return toCommentResponse(comment);
    }

    public List<CommentResponse> getComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(this::toCommentResponse).toList();
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }

    private TaskResponse toResponse(Task t) {
        List<CommentResponse> comments = t.getComments().stream()
                .map(this::toCommentResponse).toList();
        return TaskResponse.builder()
                .id(t.getId())
                .projectId(t.getProjectId())
                .columnId(t.getColumnId())
                .title(t.getTitle())
                .description(t.getDescription())
                .assigneeId(t.getAssigneeId())
                .createdBy(t.getCreatedBy())
                .priority(t.getPriority().name())
                .dueDate(t.getDueDate())
                .position(t.getPosition())
                .comments(comments)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private CommentResponse toCommentResponse(TaskComment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .taskId(c.getTask().getId())
                .userId(c.getUserId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
