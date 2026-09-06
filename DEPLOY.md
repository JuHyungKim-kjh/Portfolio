# 배포 — Docker · ECR · EC2 · GitHub Actions

`main` 에 푸시하면 **테스트 → 이미지 빌드 → ECR 푸시 → EC2 재기동 → 실제 화면 확인**까지 자동으로 간다.
아래 3·6장은 그 자동화가 막혔을 때 손으로 같은 일을 하는 길이다.

```
개발 PC                                   AWS
──────                                    ───
git push main  ──▶ GitHub Actions  ──▶  ECR (portfolio)
                          │                    │
                          └─ SSM Run Command ──▶ EC2 ┌─ portfolio-app (8080)
                                                     └─ 호스트 80 → 컨테이너 8080
```

---

## 0. 먼저 알아야 할 것 두 가지

**① 이 서비스는 상태가 없다.** DB 도, 호스트에 붙이는 데이터 파일도, API 키도 쓰지 않는다.
프로젝트 정보는 전부 `src/main/resources/projects.json` 이라 **이미지 안에 들어간다** —
프로젝트를 추가하면 이미지를 다시 만들어야 반영된다(푸시하면 자동으로 된다).
담다처럼 "앱과 데이터의 교체 주기를 나누는" 구성이 여기서는 필요 없다.

**② 배포본만 Thymeleaf 캐시를 켠다.** `application.yaml` 의 `thymeleaf.cache: false` 는
개발 편의용 임시 설정이고, compose 가 `SPRING_THYMELEAF_CACHE=true` 를 넘겨 배포본에서만 켠다.
환경변수가 yaml 보다 우선하므로 **코드는 갈라지지 않는다.**

---

## 1. 준비물

| | |
|---|---|
| AWS 계정 | ECR·EC2 를 만들 수 있는 IAM 사용자 |
| 로컬 | Docker Desktop(켜져 있어야 한다) · `aws` CLI v2 · git · `gh` |
| 리전 | `ap-northeast-2`(서울) 기준으로 적었다 |

```powershell
aws configure                    # Access Key / Secret / ap-northeast-2 / json
aws sts get-caller-identity      # 계정 번호가 나오면 준비 완료
```

**로컬 IAM 사용자 권한**: `AmazonEC2ContainerRegistryPowerUser`

---

## 2. EC2 만들기

| 항목 | 값 | 왜 |
|---|---|---|
| AMI | **Ubuntu LTS** 또는 **Amazon Linux 2023** | `ec2-bootstrap.sh` 가 둘 다 가린다(`apt` / `dnf`). ⚠ 접속 사용자명이 다르다 — Ubuntu 는 `ubuntu`, AL2023 은 `ec2-user` |
| 아키텍처 | **x86_64** | 이미지를 `linux/amd64` 로 만든다. `t4g`(arm64)를 고르면 `exec format error` 로 죽는다 |
| 인스턴스 | **t3.micro(1GB)** | DB 가 없어 앱 하나만 뜬다. 부트스트랩이 스왑 2GB 를 만든다 |
| 스토리지 | 16GB gp3 | 이미지 두 벌 + 로그 여유 |
| 키 페어 | 새로 만들어 `.pem` 보관 | 초기 설정용 SSH |

**보안 그룹 인바운드**

| 포트 | 소스 | |
|---|---|---|
| 22 | **내 IP** | SSH. `0.0.0.0/0` 으로 열지 말 것 |
| **80** | `0.0.0.0/0` | 서비스 — 마법사의 "HTTP 트래픽 허용" 체크로도 된다 |

자동 배포는 SSM 이라 **인바운드가 더 필요 없다**(8장).

**IAM 역할** `portfolio-ec2-role` — 정책 두 개를 함께 붙인다.

| 정책 | 왜 |
|---|---|
| `AmazonEC2ContainerRegistryReadOnly` | 없으면 EC2 안에서 `aws ecr get-login-password` 가 자격 증명을 못 찾는다 |
| `AmazonSSMManagedInstanceCore` | 없으면 에이전트가 SSM 에 **등록되지 않아** 자동 배포가 `InvalidInstanceId` 로 즉시 실패한다. 보안이 아니라 기능 요건이다 |

> **탄력적 IP 를 붙인다.** 이력서에 적는 주소다. 실행 중인 인스턴스에 붙어 있는 동안은 무료이고,
> 붙이지 않으면 인스턴스를 껐다 켤 때마다 퍼블릭 IP 가 바뀐다.
> ⚠ 인스턴스를 종료하면 주소가 떨어져 나와 **연결 안 된 상태로 과금**된다 — 같이 릴리스할 것.

---

## 3. 이미지 만들어 올리기 (개발 PC · 우회로)

```bash
AWS_REGION=ap-northeast-2 ./deploy/build-and-push.sh
```

끝나면 이미지 주소를 찍어 준다. 그 줄을 6장의 `.env` 에 넣는다.

> ⚠ **올리기 전에 로컬에서 테스트를 돌린다.** 이미지 빌드 안에서는 테스트를 돌리지 않는다 —
> GitHub Actions 가 이미지 빌드보다 앞에서 돌리기 때문이다(8장).
> ```powershell
> .\gradlew.bat test
> ```

---

## 4. 올리기 전에 로컬에서 통째로 돌려 보기

EC2 로 나가기 전에 이 PC 에서 한 번 띄워 본다. 실패를 EC2 가 아니라 여기서 보는 편이 훨씬 싸다.

```powershell
docker build --platform linux/amd64 -t portfolio:local .
docker run --rm -p 8081:8080 -e SPRING_THYMELEAF_CACHE=true portfolio:local
```

`http://localhost:8081/` 에서 확인한다.

| | |
|---|---|
| `/` | 인트로 → 개인 → 팀 세로 스냅 스크롤, 슬라이드 넘김 |
| `/personal` · `/team` | 목록 |
| `/projects/cinepick` · `/projects/damda` | 상세. **담다에만** `My Role` 구역이 있다 |
| `/css/style.css.bak` | **404 여야 한다** — `static` 아래 파일은 그대로 서빙된다 |
| `/projects/nope` | 404 |

실측(2026-09-07 · Docker Desktop for Windows):

| | |
|---|---|
| 이미지 | **476MB** (`eclipse-temurin:17-jre` 기반) |
| 기동 | **2.5초** — 적재할 데이터가 없다(담다는 35초였다) |
| 실행 사용자 | `uid=999(portfolio)` — root 가 아니다 |
| HEALTHCHECK | `start-period` 30초 · TCP 8080. `docker ps` 가 `(healthy)` 로 바뀌면 확인 완료 |

---

## 5. 자동 배포 준비물 (한 번만)

| 어디 | 무엇 | 왜 |
|---|---|---|
| IAM 역할 `portfolio-ec2-role` | `AmazonEC2ContainerRegistryReadOnly` + **`AmazonSSMManagedInstanceCore`** | 2장 |
| IAM 사용자 `portfolio-gha` | `AmazonEC2ContainerRegistryPowerUser` + 아래 인라인 정책 | ECR 푸시와 SSM 명령 전송 |
| 저장소 Secret | `AWS_ACCESS_KEY_ID` · `AWS_SECRET_ACCESS_KEY` | |
| 저장소 **Variable** | `EC2_INSTANCE_ID` | Secrets 에 넣어도 동작하지만 Variables 가 제자리다 — Secrets 는 로그에서 `***` 로 가려져 디버깅이 어렵다 |

인라인 정책 — ⚠ `AmazonSSMFullAccess` 를 쓰지 말 것. 그 키로 **계정의 모든 EC2 에 임의 명령**을
보낼 수 있게 된다. 인스턴스 하나로 좁힌다.
⚠ IAM 정책 문서는 **출력 가능한 ASCII 만** 받는다 — 자리표시자에 한글을 남기면
`The specified value for policyDocument is invalid` 로 거부된다.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ssm:SendCommand",
      "Resource": [
        "arn:aws:ec2:ap-northeast-2:000000000000:instance/i-0000000000000000",
        "arn:aws:ssm:ap-northeast-2::document/AWS-RunShellScript"
      ]
    },
    {
      "Effect": "Allow",
      "Action": ["ssm:GetCommandInvocation", "ssm:ListCommandInvocations"],
      "Resource": "*"
    }
  ]
}
```

> ⚠ **인스턴스를 다시 만들면** 역할 연결·저장소 Variable·이 정책의 인스턴스 ARN 을 **함께** 고쳐야 한다.

---

## 6. EC2 준비하고 띄우기

```bash
# 로컬에서 — 배포 파일 일습을 올린다
scp -i portfolio.pem -r deploy/* ubuntu@<IP>:/tmp/

# EC2 에서
ssh -i portfolio.pem ubuntu@<IP>
bash /tmp/ec2-bootstrap.sh           # Docker 설치 · 스왑 · 디렉터리 (한 번만)
newgrp docker                        # docker 그룹 반영 (또는 재로그인)

cp /tmp/docker-compose.yml /tmp/.env.example /tmp/deploy.sh /opt/portfolio/
cd /opt/portfolio
cp .env.example .env
vi .env                              # ECR_IMAGE 한 줄만 채운다

bash deploy.sh
```

---

## 7. 확인

```bash
docker compose --env-file .env ps       # STATUS 가 healthy
docker compose --env-file .env logs -f app
```

브라우저에서 `http://<IP>/` 부터 상세까지 4장과 같은 경로를 확인한다.

---

## 8. 자동 배포 (GitHub Actions)

워크플로는 `.github/workflows/deploy.yml` 이고, 3장·6장에서 손으로 하던 것을 그대로 부른다.

```
git push origin main
      ↓
GitHub Actions
  ① gradlew test              ← 깨지면 여기서 멈춘다. 이미지도 안 올라간다
  ② docker build (amd64)
  ③ ECR 푸시  portfolio:latest · portfolio:<짧은 sha>
      ↓ SSM Run Command
EC2  ④ bash /opt/portfolio/deploy.sh <이미지>
        → .env 갱신 → ECR pull → compose up → 기동 확인(최대 120초)
```

### 왜 SSH 가 아니라 SSM 인가

GitHub 러너의 IP 는 수십만 개라, SSH 로 배포하려면 **22번을 사실상 `0.0.0.0/0` 으로 열어야** 한다.
SSM 은 EC2 안의 에이전트가 AWS 로 **나가서** 명령을 받아 오는 구조라 **들어오는 포트를 하나도
열지 않는다.** SSH 개인키를 저장소 시크릿에 넣지 않아도 되는 것은 덤이다.

### 되돌리기

Actions 탭 → **Run workflow** 로 예전 커밋에서 다시 돌리거나, EC2 에서 직접 지정한다.

```bash
cd /opt/portfolio && bash deploy.sh <계정>.dkr.ecr.<리전>.amazonaws.com/portfolio:<예전 sha>
```

`deploy.sh` 는 인자로 받은 이미지를 `.env` 에도 써 두므로, 다음에 인자 없이 돌려도 같은
이미지로 뜬다 — 되돌린 뒤 손으로 재기동했을 때 조용히 최신으로 되돌아가지 않는다.

### 자동화하지 않는 것

- **`.env`** — EC2 에만 둔다. 비밀정보는 없지만 `ECR_IMAGE` 는 서버의 현재 상태라 저장소가 아니라 서버가 가질 값이다.
- **무중단 배포** — 서버가 한 대라 컨테이너를 갈아끼우는 동안 몇 초 끊긴다. ⚠ 시연 직전에는 푸시하지 말 것.

---

## 9. 막혔을 때

| 증상 | 원인 |
|---|---|
| `exec format error` | arm64 EC2 에 amd64 이미지. 인스턴스를 `t3.*` 로 바꾸거나 `PLATFORM=linux/arm64` 로 빌드 |
| ECR 로그인 실패 (EC2) | 인스턴스에 `AmazonEC2ContainerRegistryReadOnly` 역할이 없다. 2장 |
| Actions 마지막 단계 `InvalidInstanceId` | 인스턴스 역할에 `AmazonSSMManagedInstanceCore` 가 없거나, 저장소 Variable `EC2_INSTANCE_ID` 가 틀렸다. 5장 |
| Actions `Permission denied ./gradlew` | 워크플로의 `chmod +x gradlew` 가 빠졌다. git 에 100644 로 들어 있다 |
| EC2 에서 `deploy.sh: /usr/bin/env: 'bash\r'` | `.sh` 가 CRLF 로 커밋됐다. `.gitattributes` 의 `*.sh text eol=lf` 를 확인 |
| `120초 안에 응답하지 않았습니다` | 컨테이너는 떴지만 앱이 안 떴다. 로그가 그 아래 그대로 찍힌다 |
| 기동 중 컨테이너가 죽음 | 메모리. `free -h` 로 스왑을 확인한다(`ec2-bootstrap.sh` 가 2GB 를 만든다) |
| 화면을 고쳤는데 그대로 | 배포본은 Thymeleaf 캐시가 켜져 있다. **이미지를 다시 만들어야** 반영된다 |
| IAM 정책이 `policyDocument is invalid` | 정책 JSON 에 한글/특수문자가 남았다. ASCII 만 허용된다. 5장 |
| 이미지 태그에 `-dirty` | 커밋 안 된 변경을 로컬에서 올렸다는 표시다. 태그로 그 커밋을 체크아웃해도 같은 이미지가 안 나온다 |
