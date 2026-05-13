package com.minijira.workspace.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String slug;
    private Long ownerId;
    private String userRole;
    private List<MemberResponse> members;
    private LocalDateTime createdAt;
}
