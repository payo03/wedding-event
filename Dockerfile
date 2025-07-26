# 1단계: Gradle 빌드 (Gradle + JDK 21)
FROM gradle:8.4-jdk21 AS builder

WORKDIR /app
COPY . . 
RUN gradle build -x test

# 2단계: 실제 Spring Boot 실행 (JDK 21)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar 

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]