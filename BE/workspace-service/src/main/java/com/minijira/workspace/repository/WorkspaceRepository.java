package com.minijira.workspace.repository;

import com.minijira.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    boolean existsBySlug(String slug);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.userId = :userId")
    List<Workspace> findAllByMemberUserId(Long userId);
}
