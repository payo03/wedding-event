# cy-wedding/back/Dockerfile (새로 생성하거나 기존 Dockerfile을 이동 후 수정)

# 1단계: Gradle 빌드 (Gradle + JDK 21)
FROM gradle:8.4-jdk21 AS builder

# 컨테이너의 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 현재 디렉토리 (back 프로젝트의 루트)의 모든 파일을 컨테이너의 /app으로 복사
COPY . .

# 백엔드 프로젝트 빌드 (테스트는 제외)
RUN gradle build -x test

# 2단계: 실제 Spring Boot 실행 (JDK 21)
FROM eclipse-temurin:21-jdk
WORKDIR /app

# builder 스테이지에서 빌드된 JAR 파일을 가져옵니다.
# 보통 build/libs/ 에 JAR 파일이 생성됩니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# EXPOSE 10000
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]