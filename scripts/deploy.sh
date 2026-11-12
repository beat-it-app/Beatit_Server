#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/app

echo "> 현재 작업 디렉토리로 이동: $REPOSITORY" >> $REPOSITORY/deploy.log
cd $REPOSITORY

# 1. .env 파일 환경변수 세션 로드 (도커 컨테이너 빌드/실행 시 주입용)
if [ -f "$REPOSITORY/.env" ]; then
  echo "> .env 파일 환경변수를 세션에 로드합니다." >> $REPOSITORY/deploy.log
  export $(cat $REPOSITORY/.env | grep -v '^#' | xargs)
fi

# 2. 실행할 Docker Compose 파일 판별 (운영: prod, 개발: dev)
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
  COMPOSE_FILE="docker-compose.prod.yaml"
else
  COMPOSE_FILE="docker-compose.dev.yaml"
fi

# 파일 존재 여부 검증 (누락 시 조기 차단)
if [ ! -f "$REPOSITORY/$COMPOSE_FILE" ]; then
  echo "> [ERROR] $COMPOSE_FILE 파일을 찾을 수 없습니다." >> $REPOSITORY/deploy.log
  exit 1
fi

echo "> $COMPOSE_FILE 기반 컨테이너 재구동 시작" >> $REPOSITORY/deploy.log
docker compose -f $COMPOSE_FILE down >> $REPOSITORY/deploy.log 2>&1
docker compose -f $COMPOSE_FILE up -d --build --remove-orphans >> $REPOSITORY/deploy.log 2>&1

# 3. 사용하지 않는 구버전 Docker 이미지 정리 (용량 확보)
echo "> Dangling Docker 이미지 정리" >> $REPOSITORY/deploy.log
docker image prune -af >> $REPOSITORY/deploy.log 2>&1

# 4. 과거 CodeDeploy 배포 누적 아카이브 자동 청소 (디스크 100% 방지)
echo "> CodeDeploy 과거 배포 이력 정리" >> $REPOSITORY/deploy.log
sudo find /opt/codedeploy-agent/deployment-root/ -mindepth 2 -maxdepth 2 -type d -mtime +1 -exec rm -rf {} + >> $REPOSITORY/deploy.log 2>&1
sudo rm -rf /opt/codedeploy-agent/deployment-root/ongoing-deployment/* >> $REPOSITORY/deploy.log 2>&1

echo "> 배포가 성공적으로 완료되었습니다." >> $REPOSITORY/deploy.log