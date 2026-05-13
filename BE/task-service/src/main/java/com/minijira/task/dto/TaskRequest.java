package com.minijira.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Column ID is required")
    private Long columnId;
    @NotNull(message = "Project ID is required")
    private Long projectId;
    private Long assigneeId;
    private String priority = "MEDIUM";
    private LocalDate dueDate;
}
