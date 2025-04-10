# MongoDB 베이스 이미지를 사용
FROM mongo:5.0

# 데이터베이스 볼륨 디렉토리
VOLUME /data/db

# 작업 디렉토리 설정
WORKDIR /data

# 초기화 스크립트를 복사
COPY mongo/mongodb_setup.js /docker-entrypoint-initdb.d/

# 환경 변수 설정 (초기 계정 및 데이터베이스)
ENV MONGO_INITDB_ROOT_USERNAME=admin
ENV MONGO_INITDB_ROOT_PASSWORD=admin
ENV MONGO_INITDB_DATABASE=notice