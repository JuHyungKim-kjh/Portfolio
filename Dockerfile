# 포트폴리오 홈페이지 이미지 — 2단계 빌드.
#
# ★ 담다와 달리 붙일 데이터가 없다. DB 도 호스트 볼륨도 쓰지 않으므로 jar 하나로 끝난다.
#   바꿀 값(포트·이메일·GitHub 주소)은 전부 환경변수라 이미지를 다시 만들 필요가 없다.

# ── 1단계: 빌드 ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

# ⚠ 의존성만 먼저 받아 레이어로 굳힌다. 소스만 고친 재빌드에서 이 레이어가 재사용된다.
#   빌드 파일이 안 바뀌면 도커가 이 줄을 건너뛰므로 재빌드가 크게 빨라진다.
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon --console=plain dependencies

COPY src src
# ⚠ 테스트는 여기서 돌리지 않는다 — GitHub Actions 가 이미지 빌드보다 <b>앞에서</b> 돌린다.
#   러너에 Gradle 캐시가 있어 빠르고, 실패를 더 이른 단계에서 잡는다.
RUN ./gradlew --no-daemon clean bootJar

# ── 2단계: 실행 ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# root 로 돌리지 않는다. 쓰는 파일이 없어 홈 디렉터리 권한만 있으면 된다.
RUN groupadd --system portfolio && useradd --system --gid portfolio --home-dir /app portfolio

# ⚠ COPY 뒤에 chown -R 을 따로 돌리지 말 것 — 레이어가 하나 더 생기면서 jar 가 이미지에
#   두 번 들어간다(담다에서 733MB → 653MB 차이를 실측했다). --chown 으로 한 번에 한다.
COPY --from=build --chown=portfolio:portfolio /src/build/libs/*.jar app.jar

USER portfolio
EXPOSE 8080

# ⚠ 한글 로그가 깨지지 않게 인코딩을 못 박는다(Java 17 은 파일 인코딩이 로캘을 따라간다).
# ★ 이 서버에는 이 앱만 뜬다(담다는 MySQL 과 나눠 써서 60% 였다). 그래도 t3.micro 1GB 에
#   메타스페이스·스택까지 얹히므로 70% 로 여유를 남긴다.
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul"

# 액추에이터가 없어 HTTP 헬스체크를 걸 수 없다. 포트가 열렸는지만 본다.
# ★ "서비스가 된다" 는 배포 쪽에서 본다 — deploy.sh 가 호스트에서 curl 로 실제 화면을 친다.
HEALTHCHECK --interval=20s --timeout=3s --start-period=30s --retries=5 \
    CMD bash -c 'echo > /dev/tcp/127.0.0.1/8080' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
