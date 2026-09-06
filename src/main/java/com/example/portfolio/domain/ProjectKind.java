package com.example.portfolio.domain;

/**
 * 프로젝트 구분. URL 조각(personal/team)과 화면 표기를 함께 들고 있다.
 */
public enum ProjectKind {

    PERSONAL("personal", "개인 프로젝트", "Personal"),
    TEAM("team", "팀 프로젝트", "Team");

    private final String path;
    private final String label;
    private final String labelEn;

    ProjectKind(String path, String label, String labelEn) {
        this.path = path;
        this.label = label;
        this.labelEn = labelEn;
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
