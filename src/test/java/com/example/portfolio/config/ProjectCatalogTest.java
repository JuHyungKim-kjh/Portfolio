package com.example.portfolio.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.portfolio.domain.Project;
import com.example.portfolio.domain.Shot;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * projects.json 에 프로젝트를 추가했을 때 조용히 깨지는 것을 막는 안전망.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProjectCatalogTest {

    @Autowired
    private ProjectCatalog catalog;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("slug 는 비어 있지 않고 서로 겹치지 않는다")
    void slugsAreUniqueAndPresent() {
        Set<String> seen = new HashSet<>();
        for (Project project : catalog.findAll()) {
            assertNotNull(project.getSlug(), "slug 가 없는 항목이 있습니다.");
            assertFalse(project.getSlug().isBlank(), "slug 가 비어 있는 항목이 있습니다.");
            assertTrue(seen.add(project.getSlug()), "slug 가 중복됩니다 — " + project.getSlug());
            assertNotNull(project.getKind(), "kind 가 없습니다 — " + project.getSlug());
        }
    }

    @Test
    @DisplayName("모든 화면 이미지가 static 아래에 실제로 존재한다")
    void everyShotImageExists() {
        for (Project project : catalog.findAll()) {
            for (Shot shot : project.getShots()) {
                String path = "static" + shot.getImage();
                assertTrue(new ClassPathResource(path).exists(),
                        project.getSlug() + " 의 이미지를 찾을 수 없습니다 — " + shot.getImage());
            }
        }
    }

    @Test
    @DisplayName("모든 프로젝트의 상세 화면이 열린다")
    void everyProjectHasDetailPage() throws Exception {
        for (Project project : catalog.findAll()) {
            mockMvc.perform(get("/projects/{slug}", project.getSlug()))
                    .andExpect(status().isOk());
        }
    }
}
