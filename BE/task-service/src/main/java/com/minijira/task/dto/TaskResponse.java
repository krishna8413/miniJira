package com.minijira.task.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class TaskResponse {
    private Long id;
    private Long projectId;
    private Long columnId;
    private String title;
    private String description;
    private Long assigneeId;
    private Long createdBy;
    private String priority;
    private LocalDate dueDate;
    private Integer position;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
