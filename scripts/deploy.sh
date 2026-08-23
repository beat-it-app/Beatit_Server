#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/app

echo "> 현재 작업 디렉토리로 이동: $REPOSITORY" >> $REPOSITORY/deploy.log
cd $REPOSITORY

# 1. .env 파일 환경변수 세션 로드 (도커 컨테이너 빌드/실행 시 주입용)
if [ -f "$REPOSITORY/.env" ]; then
  echo "> .env 파일 환경변수를 세션에 로드합니다." >> $REPOSITORY/deploy.log
  export $(cat $REPOSITORY/.env | grep -v '^#' | xargs)
fi

# 2. Docker Compose로 전체 컨테이너 다운 및 재빌드 실행
echo "> Docker Compose 재구동 시작" >> $REPOSITORY/deploy.log
docker compose down >> $REPOSITORY/deploy.log 2>&1
docker compose up -d --build >> $REPOSITORY/deploy.log 2>&1

# 3. 사용하지 않는 구버전 Docker 이미지 정리 (용량 확보)
echo "> Dangling Docker 이미지 정리" >> $REPOSITORY/deploy.log
docker image prune -f >> $REPOSITORY/deploy.log 2>&1

echo "> 배포가 성공적으로 완료되었습니다." >> $REPOSITORY/deploy.log