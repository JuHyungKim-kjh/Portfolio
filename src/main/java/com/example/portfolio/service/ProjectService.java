package com.example.portfolio.service;

import com.example.portfolio.config.ProjectCatalog;
import com.example.portfolio.domain.Project;
import com.example.portfolio.domain.ProjectKind;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectCatalog catalog;

    public List<Project> findByKind(ProjectKind kind) {
        return catalog.findAll().stream()
                .filter(project -> project.getKind() == kind)
                .toList();
    }

    public Project getBySlug(String slug) {
        return catalog.findAll().stream()
                .filter(project -> project.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new ProjectNotFoundException(slug));
    }

    /** 인트로의 배포 건수 표기에 쓴다 */
    public long countDeployed() {
        return catalog.findAll().stream()
                .filter(Project::isDeployed)
                .count();
    }
}
