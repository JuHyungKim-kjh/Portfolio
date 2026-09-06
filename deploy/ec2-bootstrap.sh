#!/usr/bin/env bash
# EC2 에서 <b>딱 한 번</b> 실행한다. Docker · docker compose · AWS CLI 를 깔고 자리를 만든다.
#
#   bash ec2-bootstrap.sh
#
# ★ 배포판을 가린다. Ubuntu(apt) 와 Amazon Linux 2023(dnf) 어느 쪽이든 끝나고 남는 상태는 같다.
#   담다에서 AMI 를 바꿀 때마다 스크립트를 고치는 것보다 여기서 갈래를 두는 편이 나았다.
set -euo pipefail

if command -v apt-get >/dev/null 2>&1; then
  # ── Ubuntu / Debian ─────────────────────────────────────────────────────────
  # ⚠ 도커는 <b>우분투 기본 저장소</b>의 docker.io 로 깐다 — Docker 사가 운영하는 apt 저장소
  #   (download.docker.com)는 우분투 릴리스별로 폴더가 갈리는데, 갓 나온 릴리스는 그 폴더가
  #   아직 없어 설치가 통째로 실패할 수 있다. 기본 저장소로 충분하고 awscli 도 v2 가 들어 있다.
  echo "▸ Ubuntu 계열 — 패키지 설치"
  sudo apt-get update -qq
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
      docker.io docker-compose-v2 awscli
else
  # ── Amazon Linux 2023 ───────────────────────────────────────────────────────
  echo "▸ Amazon Linux 계열 — 패키지 설치"
  sudo dnf install -y docker
  # AL2023 에는 compose 플러그인이 없어 따로 받는다. AWS CLI 는 기본 탑재다.
  sudo mkdir -p /usr/libexec/docker/cli-plugins
  sudo curl -fsSL \
    "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
    -o /usr/libexec/docker/cli-plugins/docker-compose
  sudo chmod +x /usr/libexec/docker/cli-plugins/docker-compose
fi

echo "▸ Docker 기동"
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"

# ⚠ t3.micro 는 1GB 다. 앱 하나뿐이라 평소에는 남지만, 이미지 빌드 없이 pull 만 해도
#   기동 순간에 잠깐 몰린다. 스왑은 넣어 두면 손해가 없다.
if [ ! -f /swapfile ]; then
  echo "▸ 스왑 2GB 생성"
  sudo dd if=/dev/zero of=/swapfile bs=1M count=2048 status=none
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile >/dev/null
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab >/dev/null
fi

echo "▸ 배포 디렉터리 생성"
sudo mkdir -p /opt/portfolio
sudo chown -R "$USER":"$USER" /opt/portfolio

echo
echo "설치 확인"
docker --version
docker compose version
aws --version
free -h | head -2

cat <<'MSG'

완료. 다음으로 할 일:

  1) docker-compose.yml · .env.example · deploy.sh 를 /opt/portfolio 에 올린다
  2) newgrp docker   (또는 재로그인 — docker 그룹 반영)
  3) cd /opt/portfolio && cp .env.example .env && vi .env   (ECR_IMAGE 한 줄)
  4) bash deploy.sh

⚠ 이 인스턴스에 IAM 역할(portfolio-ec2-role)이 붙어 있어야 deploy.sh 의 ECR 로그인이 된다.
  그 역할에는 AmazonEC2ContainerRegistryReadOnly 와 AmazonSSMManagedInstanceCore 가
  함께 있어야 한다 — 뒤엣것이 없으면 GitHub Actions 자동 배포가 InvalidInstanceId 로 실패한다.
  확인:
      curl -s http://169.254.169.254/latest/meta-data/iam/security-credentials/
MSG
