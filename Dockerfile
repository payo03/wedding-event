# 1단계: Gradle 빌드 (Gradle + JDK 21)
FROM gradle:8.4-jdk21 AS builder

# Node.js 및 npm 설치
# apt 패키지 목록 업데이트 및 nodejs, npm 설치
# 설치 후 캐시를 정리하여 최종 이미지 크기를 줄입니다.
RUN apt-get update && \
    apt-get install -y nodejs npm && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . . 
RUN gradle build -x test

# 2단계: 실제 Spring Boot 실행 (JDK 21)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar 

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]