# Base Image
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /srunners

# Gradle 빌드 결과물 복사
COPY build/libs/notice-service-0.0.1-SNAPSHOT.jar srunners-notice.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "srunners-notice.jar"]