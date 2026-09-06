package com.example.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 사이트 주인 정보. application.yaml 의 portfolio.owner 아래를 바인딩한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "portfolio.owner")
public class OwnerProperties {

    private String name;
    /** 좌측 레일 마크에 쓰는 이니셜 */
    private String initials;
    private String role;
    private String intro;
    private String email;
    private String github;

    /**
     * 주소가 있을 때만 칩을 보여준다 — {@link com.example.portfolio.domain.Project#isRepoLinked()} 와 같은 규칙이다.
     * 비공개 저장소로 보내면 방문자에게는 404 라, 링크가 있는 편이 없는 것보다 나쁘다.
     * 공개로 돌릴 때는 application.yaml 의 값이나 OWNER_GITHUB 환경변수만 채우면 다시 나온다.
     */
    public boolean isGithubLinked() {
        return github != null && !github.isBlank();
    }
}
