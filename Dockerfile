# 1. Base Image 설정: OpenJDK 17 사용 (Slim 버전으로 크기 최적화)
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle 빌드 결과물 복사
# build/libs 경로에 만들어진 .jar 파일을 새 컨테이너의 /app 디렉토리로 복사
COPY build/libs/*.jar app.jar

# 4. 컨테이너 실행 시 기본 명령어 설정
# "app.jar"를 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
