#!/usr/bin/env bash
# 로컬(개발 PC)에서 실행한다 — 이미지를 만들어 ECR 에 올린다.
#
#   AWS_REGION=ap-northeast-2 ./deploy/build-and-push.sh
#
# ★ 평소에는 GitHub Actions 가 같은 일을 한다. 이 스크립트는 자동화가 막혔을 때의 우회로이고,
#   첫 배포에서 "파이프라인 문제인지 앱 문제인지" 를 가르는 데도 쓴다.
set -euo pipefail

AWS_REGION="${AWS_REGION:-ap-northeast-2}"
# ⚠ AWS 콘솔/CLI 에서 만든 리포지토리 이름과 같아야 한다.
#   다르면 스크립트가 없는 줄 알고 새로 만들어 리포지토리가 둘로 갈린다.
ECR_REPO="${ECR_REPO:-portfolio}"
# ⚠ 담다는 backend/ 가 컨텍스트였지만 이 저장소는 단일 모듈이라 <b>저장소 루트</b>다.
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# ⚠ EC2 가 x86_64(t3.*) 라면 반드시 linux/amd64 로 만들어야 한다. Apple Silicon 이나
#   arm64 머신에서 그냥 빌드하면 EC2 에서 "exec format error" 로 죽는다.
PLATFORM="${PLATFORM:-linux/amd64}"

command -v aws    >/dev/null || { echo "aws CLI 가 없습니다."   >&2; exit 1; }
command -v docker >/dev/null || { echo "docker 가 없습니다."     >&2; exit 1; }
docker info >/dev/null 2>&1  || { echo "Docker 데몬이 꺼져 있습니다 — Docker Desktop 을 켜세요." >&2; exit 1; }

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
REGISTRY="${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
# 되돌릴 수 있게 커밋 해시로도 태그를 남긴다. latest 하나만 쓰면 어느 코드가 떠 있는지 알 수 없다.
# ★ 형식을 워크플로(rev-parse --short)와 맞춰 ECR 목록에서 섞이지 않게 한다.
TAG="$(git -C "$PROJECT_DIR" rev-parse --short HEAD)"

# ★ 커밋 안 된 변경이 있으면 태그에 -dirty 를 붙인다.
#   해시 태그의 값어치는 "이 태그를 보면 그 커밋을 그대로 체크아웃할 수 있다" 는 데 있는데,
#   작업트리가 지저분한 채로 올리면 <b>태그가 거짓말을 한다</b> — 같은 태그로 서로 다른
#   이미지가 생기고, 되돌릴 때 무엇으로 돌아가는지 알 수 없어진다.
#   막지 않고 표시만 하는 이유는 급할 때 올리는 길을 남겨 두기 위해서다.
if [ -n "$(git -C "$PROJECT_DIR" status --porcelain --untracked-files=no)" ]; then
  TAG="${TAG}-dirty"
  echo "⚠ 커밋 안 된 변경이 있습니다 — 태그를 ${TAG} 로 답니다." >&2
fi

echo "▸ 리포지토리 확인 (${ECR_REPO} @ ${AWS_REGION})"
aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 \
  || aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION" \
        --image-scanning-configuration scanOnPush=true >/dev/null

echo "▸ ECR 로그인"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$REGISTRY"

echo "▸ 이미지 빌드 (${PLATFORM})"
docker build --platform "$PLATFORM" \
  -t "${REGISTRY}/${ECR_REPO}:${TAG}" \
  -t "${REGISTRY}/${ECR_REPO}:latest" \
  "$PROJECT_DIR"

echo "▸ 푸시"
docker push "${REGISTRY}/${ECR_REPO}:${TAG}"
docker push "${REGISTRY}/${ECR_REPO}:latest"

cat <<MSG

완료. EC2 의 /opt/portfolio/.env 에 이 줄을 넣으세요:

  ECR_IMAGE=${REGISTRY}/${ECR_REPO}:${TAG}

(되돌리려면 예전 커밋 해시 태그로 바꾸고 deploy.sh 를 다시 돌리면 됩니다)
MSG
