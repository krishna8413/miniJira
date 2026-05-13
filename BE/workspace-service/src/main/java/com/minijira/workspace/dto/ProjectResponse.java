package com.minijira.workspace.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ProjectResponse {
    private Long id;
    private Long workspaceId;
    private String name;
    private String description;
    private Long createdBy;
    private List<ColumnResponse> columns;
    private LocalDateTime createdAt;
}
