package com.example.portfolio.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 메인 레일의 소유자 GitHub 칩도 프로젝트 칩과 같은 규칙을 쓴다
 * ({@link com.example.portfolio.domain.Project#isRepoLinked()}).
 * 두 자리가 따로 놀면 한쪽만 고쳤을 때 화면에서만 어긋난다.
 */
class OwnerPropertiesTest {

    private static OwnerProperties withGithub(String github) {
        OwnerProperties owner = new OwnerProperties();
        owner.setGithub(github);
        return owner;
    }

    @Test
    @DisplayName("GitHub 주소가 없으면 링크로 보지 않는다")
    void githubNotLinkedWhenBlank() {
        assertFalse(withGithub(null).isGithubLinked(), "null 은 링크가 아니다");
        assertFalse(withGithub("").isGithubLinked(), "빈 문자열은 링크가 아니다");
        assertFalse(withGithub("   ").isGithubLinked(), "공백만 있는 값은 링크가 아니다");
    }

    @Test
    @DisplayName("GitHub 주소가 있으면 링크로 본다")
    void githubLinkedWhenPresent() {
        assertTrue(withGithub("https://github.com/JuHyungKim-kjh").isGithubLinked());
    }
}
