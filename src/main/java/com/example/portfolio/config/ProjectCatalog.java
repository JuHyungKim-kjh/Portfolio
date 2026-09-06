package com.example.portfolio.config;

import com.example.portfolio.domain.Project;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * projects.json 을 읽어 들고 있는 프로젝트 목록.
 *
 * 프로젝트 추가는 이 JSON 에 블록 하나를 넣는 것으로 끝나고, 메인·목록·상세가 함께 늘어난다.
 * 관리 화면이 필요해지면 이 클래스를 JPA repository 로 교체한다.
 */
@Component
public class ProjectCatalog {

    private static final String LOCATION = "projects.json";

    private final List<Project> projects;

    public ProjectCatalog(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource(LOCATION).getInputStream()) {
            ProjectFile file = objectMapper.readValue(in, ProjectFile.class);
            this.projects = List.copyOf(file.getProjects());
        } catch (IOException e) {
            throw new IllegalStateException(LOCATION + " 을 읽지 못했습니다.", e);
        }
        validate(this.projects);
    }

    /**
     * 항목을 추가하다 생긴 실수는 화면이 조용히 깨지는 대신 기동 단계에서 드러나야 한다.
     */
    private void validate(List<Project> projects) {
        Set<String> slugs = new HashSet<>();
        for (Project project : projects) {
            String slug = project.getSlug();
            if (slug == null || slug.isBlank()) {
                throw new IllegalStateException(LOCATION + ": slug 이 비어 있는 항목이 있습니다.");
            }
            if (project.getKind() == null) {
                throw new IllegalStateException(LOCATION + ": kind 가 없습니다 (PERSONAL 또는 TEAM) — " + slug);
            }
            if (!slugs.add(slug)) {
                throw new IllegalStateException(LOCATION + ": slug 이 중복됩니다 — " + slug);
            }
        }
    }

    public List<Project> findAll() {
        return projects;
    }

    @Getter
    @Setter
    static class ProjectFile {
        private List<Project> projects = List.of();
    }
}
