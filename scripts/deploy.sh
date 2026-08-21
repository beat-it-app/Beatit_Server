#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/app

echo "> 현재 작업 디렉토리로 이동: $REPOSITORY" >> $REPOSITORY/deploy.log
cd $REPOSITORY

# 1. 빌드 파일 탐색 (Spring Boot 빌드 시 생성되는 plain.jar는 제외)
BUILD_JAR=$(ls $REPOSITORY/build/libs/*.jar | grep -v 'plain')
JAR_NAME=$(basename $BUILD_JAR)
echo "> build 파일명: $JAR_NAME" >> $REPOSITORY/deploy.log

# 2. 빌드 파일 복사
DEPLOY_PATH=$REPOSITORY/
echo "> build 파일 복사: $BUILD_JAR -> $DEPLOY_PATH" >> $REPOSITORY/deploy.log
cp $BUILD_JAR $DEPLOY_PATH

# 3. 현재 구동 중인 서버 프로세스 확인 및 안전 종료
echo "> 현재 실행중인 애플리케이션 pid 확인" >> $REPOSITORY/deploy.log
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> $REPOSITORY/deploy.log
else
  echo "> kill -15 $CURRENT_PID" >> $REPOSITORY/deploy.log
  kill -15 $CURRENT_PID
  sleep 5
fi

# 4. 신규 버전 배포 실행
DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "> DEPLOY_JAR 배포 ($DEPLOY_JAR)" >> $REPOSITORY/deploy.log

# .env 파일에서 주석을 제외한 환경변수들을 쉘 세션에 강제 로드(export)합니다.
if [ -f "$REPOSITORY/.env" ]; then
  echo "> .env 파일 환경변수를 세션에 로드합니다." >> $REPOSITORY/deploy.log
  export $(cat $REPOSITORY/.env | grep -v '^#' | xargs)
fi

# 실행 경로(cd $REPOSITORY)에서 nohup으로 구동하며, dev 프로파일을 활성화합니다.
# TODO : 운영할 땐 --spring.profiles.active=prod 로 바꿔서 하기
nohup java -jar $DEPLOY_JAR --spring.profiles.active=dev >> $REPOSITORY/deploy.log 2>> $REPOSITORY/deploy_err.log &