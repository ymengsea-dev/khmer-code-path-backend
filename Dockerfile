# ---- Build stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY khmer-code-path-api ./khmer-code-path-api
COPY khmer-code-path-commons ./khmer-code-path-commons

RUN chmod +x gradlew \
    && ./gradlew :khmer-code-path-api:bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /app/khmer-code-path-api/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

# Keys: application-prod.yml  |  Values: .env on the server
#   docker run -d -p 8080:8080 --env-file .env kcp-api
#   or mount:  -v /path/to/.env:/app/.env
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
