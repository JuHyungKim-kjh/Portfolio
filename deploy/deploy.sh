#!/usr/bin/env bash
# EC2 에서 실행한다 — 새 이미지를 받아 다시 띄운다. 릴리스마다 이것만 돌리면 된다.
#
#   cd /opt/portfolio && bash deploy.sh                 # .env 의 ECR_IMAGE 를 그대로 쓴다
#   cd /opt/portfolio && bash deploy.sh <이미지주소>     # 그 이미지로 바꿔 배포한다
#
# ★ GitHub Actions 가 SSM Run Command 로 부르는 대상이 이 스크립트다(.github/workflows/deploy.yml).
#   자동 배포로 넘어가면서 바뀐 것은 "누가 부르는가" 뿐이고, 인자 없이 부르면 수동 배포 때와
#   똑같이 동작한다 — 자동화가 막혔을 때 손으로 돌릴 길을 남겨 두려는 것이다.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"
[ -f .env ] || { echo ".env 가 없습니다 — .env.example 을 복사해 채우세요." >&2; exit 1; }

# ── 인자로 받은 이미지를 .env 에 반영한다 ─────────────────────────────────────
# ⚠ .env 를 고치는 이유: 다음에 인자 없이 돌려도 같은 이미지로 뜬다. 인자만 받고 .env 를
#   안 고치면, 자동 배포 뒤에 손으로 재기동했을 때 조용히 옛 이미지로 돌아간다.
if [ $# -ge 1 ]; then
  echo "▸ 이미지 지정: $1"
  sed -i "s|^ECR_IMAGE=.*|ECR_IMAGE=$1|" .env
fi

# shellcheck disable=SC1091
set -a; . ./.env; set +a

: "${ECR_IMAGE:?.env 의 ECR_IMAGE 가 비어 있습니다}"

REGISTRY="${ECR_IMAGE%%/*}"
AWS_REGION="${AWS_REGION:-$(echo "$REGISTRY" | cut -d. -f4)}"

echo "▸ ECR 로그인 (${AWS_REGION})"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

echo "▸ 이미지 받기"
docker compose --env-file .env pull

echo "▸ 재기동"
docker compose --env-file .env up -d

# ⚠ 예전 이미지를 다 지우지는 않는다(-a 를 쓰지 않는 이유) — 되돌릴 때 받아 둔 것이 있으면 빠르다.
docker image prune -f >/dev/null

# ── 기동 확인 ────────────────────────────────────────────────────────────────
# ★ "컨테이너가 떴다" 와 "서비스가 된다" 는 다르다. compose up 은 즉시 돌아오지만 앱은
#   몇 초 뒤에야 응답한다. 여기서 안 기다리면 자동 배포가 깨진 배포를 성공으로 보고한다.
#
# ★ / 를 쓰는 이유 — 로그인이 없고 projects.json 을 읽어 렌더까지 끝나야 200 이 된다.
#   액추에이터가 없는 지금 가장 정직한 신호다.
#   ⚠ 포트는 호스트 80 이다(compose 가 80:8080 으로 매핑한다). 컨테이너 안 8080 이 아니다.
#   ⚠ 담다는 데이터 적재 때문에 180초였다. 여기는 적재가 없어 보통 10초 안에 끝난다.
echo "▸ 기동 확인 (최대 120초)"
for i in $(seq 1 40); do
  if curl -sf -o /dev/null --max-time 5 http://127.0.0.1/; then
    echo "  준비됨 ($((i * 3))초)"
    docker compose --env-file .env ps
    echo
    echo "떠 있는 이미지: ${ECR_IMAGE}"
    exit 0
  fi
  sleep 3
done

echo "120초 안에 응답하지 않았습니다. 마지막 로그 50줄:" >&2
docker logs --tail 50 portfolio-app >&2 || true
exit 1
