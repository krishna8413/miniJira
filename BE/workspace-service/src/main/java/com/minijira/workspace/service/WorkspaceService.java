package com.minijira.workspace.service;

import com.minijira.workspace.dto.*;
import com.minijira.workspace.entity.*;
import com.minijira.workspace.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnRepository columnRepository;

    @Transactional
    public WorkspaceResponse create(WorkspaceRequest request, Long userId) {
        String slug = generateSlug(request.getName());

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .slug(slug)
                .ownerId(userId)
                .build();

        workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .userId(userId)
                .role(WorkspaceMember.Role.ADMIN)
                .build();

        memberRepository.save(ownerMember);
        return toResponse(workspace, WorkspaceMember.Role.ADMIN.name());
    }

    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        return workspaceRepository.findAllByMemberUserId(userId).stream()
                .map(w -> {
                    String role = memberRepository.findByWorkspaceIdAndUserId(w.getId(), userId)
                            .map(m -> m.getRole().name())
                            .orElse("MEMBER");
                    return toResponse(w, role);
                })
                .toList();
    }

    public WorkspaceResponse getById(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        assertMember(workspaceId, userId);
        String role = memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(m -> m.getRole().name()).orElse("MEMBER");
        return toResponse(workspace, role);
    }

    @Transactional
    public void addMember(Long workspaceId, MemberRequest request, Long requesterId) {
        assertRole(workspaceId, requesterId, WorkspaceMember.Role.ADMIN);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.getUserId())) {
            throw new RuntimeException("User is already a member");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .userId(request.getUserId())
                .role(WorkspaceMember.Role.valueOf(request.getRole().toUpperCase()))
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long workspaceId, Long userId, Long requesterId) {
        assertRole(workspaceId, requesterId, WorkspaceMember.Role.ADMIN);
        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        memberRepository.delete(member);
    }

    @Transactional
    public ProjectResponse createProject(Long workspaceId, ProjectRequest request, Long userId) {
        assertMember(workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Project project = Project.builder()
                .workspace(workspace)
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(userId)
                .build();

        projectRepository.save(project);
        createDefaultColumns(project);
        return toProjectResponse(project);
    }

    public List<ProjectResponse> getProjects(Long workspaceId, Long userId) {
        assertMember(workspaceId, userId);
        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toProjectResponse)
                .toList();
    }

    private void createDefaultColumns(Project project) {
        List<String> defaults = List.of("To Do", "In Progress", "In Review", "Done");
        for (int i = 0; i < defaults.size(); i++) {
            BoardColumn col = BoardColumn.builder()
                    .project(project)
                    .name(defaults.get(i))
                    .position(i)
                    .build();
            columnRepository.save(col);
        }
    }

    private void assertMember(Long workspaceId, Long userId) {
        if (!memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new RuntimeException("Access denied: not a member of this workspace");
        }
    }

    private void assertRole(Long workspaceId, Long userId, WorkspaceMember.Role required) {
        WorkspaceMember member = memberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        if (member.getRole().ordinal() > required.ordinal()) {
            throw new RuntimeException("Insufficient permissions");
        }
    }

    private String generateSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9]+", "-");
        String slug = base;
        int count = 1;
        while (workspaceRepository.existsBySlug(slug)) {
            slug = base + "-" + count++;
        }
        return slug;
    }

    private WorkspaceResponse toResponse(Workspace w, String role) {
        List<MemberResponse> members = w.getMembers().stream()
                .map(m -> MemberResponse.builder()
                        .userId(m.getUserId())
                        .role(m.getRole().name())
                        .build())
                .toList();
        return WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .slug(w.getSlug())
                .ownerId(w.getOwnerId())
                .userRole(role)
                .members(members)
                .createdAt(w.getCreatedAt())
                .build();
    }

    private ProjectResponse toProjectResponse(Project p) {
        List<ColumnResponse> cols = p.getColumns().stream()
                .map(c -> ColumnResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .position(c.getPosition())
                        .build())
                .toList();
        return ProjectResponse.builder()
                .id(p.getId())
                .workspaceId(p.getWorkspace().getId())
                .name(p.getName())
                .description(p.getDescription())
                .createdBy(p.getCreatedBy())
                .columns(cols)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
