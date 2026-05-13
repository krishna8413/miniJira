package com.minijira.workspace.controller;

import com.minijira.workspace.dto.*;
import com.minijira.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody WorkspaceRequest request,
                                                     Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.create(request, getUserId(auth)));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(Authentication auth) {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(getUserId(auth)));
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getById(@PathVariable Long workspaceId,
                                                      Authentication auth) {
        return ResponseEntity.ok(workspaceService.getById(workspaceId, getUserId(auth)));
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long workspaceId,
                                           @Valid @RequestBody MemberRequest request,
                                           Authentication auth) {
        workspaceService.addMember(workspaceId, request, getUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long workspaceId,
                                              @PathVariable Long userId,
                                              Authentication auth) {
        workspaceService.removeMember(workspaceId, userId, getUserId(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workspaceId}/projects")
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long workspaceId,
                                                          @Valid @RequestBody ProjectRequest request,
                                                          Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createProject(workspaceId, request, getUserId(auth)));
    }

    @GetMapping("/{workspaceId}/projects")
    public ResponseEntity<List<ProjectResponse>> getProjects(@PathVariable Long workspaceId,
                                                              Authentication auth) {
        return ResponseEntity.ok(workspaceService.getProjects(workspaceId, getUserId(auth)));
    }

    private Long getUserId(Authentication auth) {
        return (Long) auth.getCredentials();
    }
}
