package com.minijira.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkspaceRequest {
    @NotBlank(message = "Workspace name is required")
    private String name;
}
