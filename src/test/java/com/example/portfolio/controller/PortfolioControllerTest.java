package com.example.portfolio.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("메인은 개인·팀 프로젝트를 모두 렌더한다")
    void index() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("CinePick")))
                .andExpect(content().string(containsString("담다")));
    }

    @Test
    @DisplayName("개인 프로젝트 목록에 팀 프로젝트가 섞이지 않는다")
    void personalList() throws Exception {
        mockMvc.perform(get("/personal"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/list"))
                .andExpect(content().string(containsString("CinePick")))
                .andExpect(content().string(not(containsString("담다"))));
    }

    @Test
    @DisplayName("팀 프로젝트 목록은 담다를 포함한다")
    void teamList() throws Exception {
        mockMvc.perform(get("/team"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/list"))
                .andExpect(content().string(containsString("담다")));
    }

    @Test
    @DisplayName("상세는 배포 링크와 주요 화면을 보여준다")
    void detail() throws Exception {
        mockMvc.perform(get("/projects/cinepick"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(content().string(containsString("http://13.124.164.67/")))
                .andExpect(content().string(containsString("/images/cinepick/01-main.jpg")));
    }

    @Test
    @DisplayName("저장소 주소가 없으면 GitHub 칩을 그리지 않는다")
    void hidesGithubChipWhenRepoIsPrivate() throws Exception {
        // 스택에 "GitHub Actions" 가 있으므로 칩 고유 문구로 좁혀서 본다
        mockMvc.perform(get("/projects/cinepick"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("GitHub ↗"))));
    }

    @Test
    @DisplayName("팀 프로젝트 상세에는 담당 업무 구역이 붙는다")
    void teamDetailHasMyRole() throws Exception {
        mockMvc.perform(get("/projects/damda"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My Role")))
                .andExpect(content().string(containsString("배포 · CI/CD")));
    }

    @Test
    @DisplayName("개인 프로젝트 상세에는 담당 업무 구역이 없다")
    void personalDetailHasNoMyRole() throws Exception {
        mockMvc.perform(get("/projects/cinepick"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("My Role"))));
    }

    @Test
    @DisplayName("없는 프로젝트는 404 다")
    void unknownProject() throws Exception {
        mockMvc.perform(get("/projects/no-such-project"))
                .andExpect(status().isNotFound());
    }
}
