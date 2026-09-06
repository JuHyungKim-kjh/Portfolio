package com.example.portfolio.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 포트폴리오에 싣는 프로젝트 하나. projects.json 한 블록이 이 객체가 된다.
 */
@Getter
@Setter
public class Project {

    private String slug;
    private String name;
    private ProjectKind kind;

    /** 화면 구성을 미리 보기 위한 예시 데이터 여부. 실제 프로젝트는 false 다. */
    private boolean sample;

    private String tagline;
    private String period;
    private String color;

    private String liveUrl;
    private String repoUrl;
    /** 발표자료(PDF). 자료가 있는 프로젝트만 채운다 — static/files/{slug}-deck.pdf 규칙 */
    private String deckUrl;

    /** 메인·목록에 쓰는 짧은 소개 */
    private String summary;
    /** 목록·상세에 쓰는 긴 소개. 한 항목이 한 문단이며, 화면에서 문단마다 &lt;p&gt; 로 그린다. */
    private List<String> description = List.of();

    /** 팀 프로젝트에서 내가 맡은 부분. 개인 프로젝트는 비워 두고, 비어 있으면 상세에서 구역이 통째로 빠진다. */
    private List<Note> contributions = List.of();

    private List<String> stack = List.of();
    private List<Note> features = List.of();
    private List<Note> issues = List.of();
    private List<Shot> shots = List.of();

    public boolean isDeployed() {
        return liveUrl != null && !liveUrl.isBlank();
    }

    /** 비공개 저장소는 링크를 걸어도 방문자에게 404 라, 주소가 있을 때만 칩을 보여준다 */
    public boolean isRepoLinked() {
        return repoUrl != null && !repoUrl.isBlank();
    }

    /** 발표자료가 없는 프로젝트가 더 많을 수 있으므로, 있을 때만 칩을 보여준다 */
    public boolean isDeckLinked() {
        return deckUrl != null && !deckUrl.isBlank();
    }

    /** 화면이 아직 없는 프로젝트도 틀은 보이도록 자리표시자 한 장을 대신 준다 */
    public List<Shot> getShots() {
        return shots.isEmpty() ? List.of(Shot.placeholder("화면 준비 중")) : shots;
    }

    /** 메인 슬라이드에 쓰는 대표 화면 한 장 */
    public Shot getCoverShot() {
        return getShots().get(0);
    }

    /** 메인 슬라이드 메타에 길게 늘어놓지 않도록 앞의 몇 개만 보여준다 */
    public List<String> getPrimaryStack() {
        return stack.size() <= 4 ? stack : stack.subList(0, 4);
    }
}
