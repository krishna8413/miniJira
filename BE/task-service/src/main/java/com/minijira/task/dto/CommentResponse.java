package com.minijira.task.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class CommentResponse {
    private Long id;
    private Long taskId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
