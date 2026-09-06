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
}
