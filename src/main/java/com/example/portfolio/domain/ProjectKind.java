package com.example.portfolio.domain;

/**
 * 프로젝트 구분. URL 조각(personal/team)과 화면 표기, 목록 머리말을 함께 들고 있다.
 */
public enum ProjectKind {

    PERSONAL("personal", "개인 프로젝트", "Personal",
            "기획부터 구현과 배포까지 혼자 진행한 프로젝트입니다. "
            + "화면과 서버, 데이터와 운영 환경까지 전부 직접 정하고 만들었습니다."),
    TEAM("team", "팀 프로젝트", "Team",
            "여러 명이 함께 만든 프로젝트입니다. "
            + "각 상세 페이지의 MY ROLE 에 제가 맡은 범위를 적어 두었습니다.");

    private final String path;
    private final String label;
    private final String labelEn;
    /** 목록 페이지 머리말. 구분마다 설명이 달라 화면이 아니라 여기에 둔다 */
    private final String lead;

    ProjectKind(String path, String label, String labelEn, String lead) {
        this.path = path;
        this.label = label;
        this.labelEn = labelEn;
        this.lead = lead;
    }

    public String getLead() {
        return lead;
    }

    public String getPath() {
        return path;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelEn() {
        return labelEn;
    }
}
