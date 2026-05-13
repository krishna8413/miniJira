package com.minijira.workspace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    private String role = "MEMBER";
}
