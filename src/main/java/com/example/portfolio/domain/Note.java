package com.example.portfolio.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 제목 한 줄 + 설명 한 문단으로 된 항목. 주요 기능과 트러블슈팅에 함께 쓴다.
 */
@Getter
@Setter
public class Note {

    private String title;
    private String body;
}
