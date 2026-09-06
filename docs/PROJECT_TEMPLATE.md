# 프로젝트 추가하기

프로젝트를 늘리는 방법은 하나다 — **`src/main/resources/projects.json` 의 `projects` 배열에 블록을 하나 더 넣는다.**
메인 슬라이드, 목록 카드, 상세 화면이 모두 이 파일에서 렌더되므로 자바 코드나 템플릿은 건드리지 않는다.

## 복붙용 블록

```json
{
  "slug": "example",
  "name": "프로젝트 이름",
  "kind": "PERSONAL",
  "sample": false,
  "tagline": "한 줄 소개 — 카드 제목 아래 앰버색으로 나온다",
  "period": "2026.09 — 2026.10 · 2개월",
  "color": "#5B6EE1",
  "liveUrl": "http://example.com/",
  "repoUrl": "https://github.com/",
  "summary": "메인 슬라이드와 목록에 쓰는 짧은 소개. 두세 문장이면 충분하다.",
  "description": [
    "목록 아래쪽과 상세의 Overview 에 들어가는 긴 설명. 한 항목이 한 문단이다.",
    "문단을 나눠 두면 상세 화면에서 문단마다 줄이 떨어져 읽기 편하다. 한 덩어리로 쓰면 15줄 넘는 벽이 된다."
  ],
  "stack": ["Java 17", "Spring Boot 4", "Thymeleaf", "MySQL"],
  "contributions": [
    { "title": "담당 범위 이름", "body": "팀 프로젝트에서 내가 맡은 부분. 개인 프로젝트는 이 배열을 넣지 않는다." }
  ],
  "features": [
    { "title": "주요 기능 이름", "body": "무엇을 할 수 있는지 한두 문장." }
  ],
  "issues": [
    { "title": "겪은 문제", "body": "무엇이 문제였고 어떻게 해결했는지." }
  ],
  "shots": [
    { "label": "메인 화면", "image": "/images/example/01-main.svg" }
  ]
}
```

## 필드

| 필드 | 설명 |
|---|---|
| `slug` | URL 조각이자 이미지 폴더 이름. `/projects/{slug}` 로 열리므로 **이미지 폴더명과 같게** 맞춘다 |
| `name` | 화면에 크게 나오는 이름 |
| `kind` | `PERSONAL` 또는 `TEAM`. 그대로 `/personal` · `/team` 갈래가 된다 |
| `sample` | `true` 면 `예시 데이터` 배지가 붙는다. 구성을 미리 보려고 넣는 가짜 항목에만 쓴다 |
| `tagline` | 한 줄 소개 |
| `period` | 자유 문자열. `2026.08 · 1개월` 처럼 기간과 소요를 함께 적는다 |
| `color` | 임시 이미지 생성 시 쓰는 대표 색. 화면 자체에는 쓰이지 않는다 |
| `liveUrl` | **비어 있으면** 버튼이 `배포 사이트 열기` 대신 `배포 준비 중` 으로 바뀐다 |
| `repoUrl` | 저장소 주소. **비워 두면 `GitHub` 칩이 아예 나오지 않는다** — 비공개 저장소는 링크를 걸어도 방문자에게 404 라 죽은 링크를 남기지 않는다 |
| `summary` | 짧은 소개. 메인 슬라이드와 목록에 쓴다 |
| `description` | **문자열 배열이고 한 항목이 한 문단이다.** 상세의 Overview 와 목록 아래쪽에 문단마다 `<p>` 로 그려진다. **프로젝트 설명만 쓴다** — 내가 무엇을 맡았는지는 `contributions` 로 뺀다 |
| `contributions` | 상세의 `My Role` 구역. **팀 프로젝트에만 채운다.** 비어 있으면 구역이 통째로 빠지므로 개인 프로젝트는 넣지 않는다 |
| `stack` | 기술 칩. 메인 슬라이드에는 **앞 4개만** 나오므로 중요한 것을 앞에 둔다 |
| `features` / `issues` | 상세의 `Features` · `Troubleshooting` 목록. 개수 제한 없음 |
| `shots` | 주요 화면. **첫 번째가 메인 슬라이드의 대표 화면**이 된다 |

## 화면 이미지

- 경로는 `src/main/resources/static` 아래 기준이다 — `/images/{slug}/01-main.svg` 는 실제로 `src/main/resources/static/images/{slug}/01-main.svg` 다.
- **실제 스크린샷은 같은 경로에 덮어쓰면 그대로 교체된다.** 확장자를 바꾸면(`.svg` → `.png`) `projects.json` 의 `image` 도 함께 고친다.
- `image` 를 비워 두거나 `shots` 를 통째로 비우면 `/images/placeholder.svg`(화면 준비 중) 가 대신 나온다. 이미지가 없다고 화면이 깨지지는 않는다.
- 경로를 잘못 적으면 `ProjectCatalogTest.everyShotImageExists` 가 빌드에서 잡는다.

## 여러 개일 때의 모습

시안 확인용으로 예시 프로젝트를 넣어 두었을 때의 구성이다. 다시 재현하려면 이 형태로 `sample: true` 항목을 넣으면 된다.

- **개인 4개 · 팀 3개** — 메인 구역의 카운터가 `Personal — 04`, 슬라이드가 `01 / 04` 로 표시된다.
- 실제 프로젝트는 **5컷**, 예시 항목은 **3컷** 씩 두었다.
- 메인 구역은 프로젝트당 대표 화면 **한 장**, 목록 카드는 그 프로젝트의 **모든 컷**을 슬라이드로 넘긴다.
- 예시 항목 예: `Logic`(알고리즘 풀이 기록) · `리드미`(독서 기록) · `Fitday`(날씨 옷차림) · `CommitLog`(팀 칸반) · `온담`(동네 나눔).

## 경계 상황에서의 동작

| 상황 | 동작 |
|---|---|
| 슬라이드가 한 장 | 화살표·도트·카운터가 통째로 숨는다 |
| 한 갈래에 프로젝트가 0개 | 구역과 좌측 레일은 유지하고 `등록된 ~가 아직 없습니다` 안내가 나온다 |
| `shots` 가 비어 있음 | 자리표시자 한 장으로 대체된다 |
| `slug` 중복 · `kind` 누락 | **기동이 중단되고** 로그에 원인이 찍힌다 (`ProjectCatalog`) |

## 확인

```powershell
.\gradlew.bat test
.\gradlew.bat bootRun
```

추가 후 `/`, `/personal` 또는 `/team`, `/projects/{slug}` 세 곳에 모두 나오는지 본다.
