package com.minijira.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskMoveRequest {
    @NotNull
    private Long targetColumnId;
    @NotNull
    private Integer newPosition;
}
