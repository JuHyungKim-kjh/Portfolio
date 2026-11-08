package com.example.portfolio.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 링크 칩을 그릴지 말지는 이 판정 하나에 달려 있다.
 *
 * <p>전에는 컨트롤러 테스트가 "repoUrl 이 빈 프로젝트" 를 projects.json 에서 빌려 와 음성
 * 케이스를 세웠는데, 저장소를 공개로 돌려 주소를 채우자 그 데이터가 사라져 테스트가 깨졌다.
 * 깨진 것은 규칙이 아니라 빌려 온 전제였다. 판정이 사는 자리에서 직접 보면 어느 프로젝트를
 * 공개로 돌리든 흔들리지 않는다.
 */
class ProjectTest {

    private static Project withRepo(String repoUrl) {
        Project project = new Project();
        project.setRepoUrl(repoUrl);
        return project;
    }

    @Test
    @DisplayName("저장소 주소가 없으면 링크로 보지 않는다")
    void repoNotLinkedWhenBlank() {
        // 비공개 저장소는 링크를 걸어도 방문자에게 404 라, 죽은 링크를 남기지 않는다
        assertFalse(withRepo(null).isRepoLinked(), "null 은 링크가 아니다");
        assertFalse(withRepo("").isRepoLinked(), "빈 문자열은 링크가 아니다");
        assertFalse(withRepo("   ").isRepoLinked(), "공백만 있는 값은 링크가 아니다");
    }

    @Test
    @DisplayName("저장소 주소가 있으면 링크로 본다")
    void repoLinkedWhenPresent() {
        assertTrue(withRepo("https://github.com/JuHyungKim-kjh/CinePick").isRepoLinked());
    }
}
