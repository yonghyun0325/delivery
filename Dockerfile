# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# 의존성 레이어를 소스코드보다 먼저 캐싱해서 재빌드 속도를 높임
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system delivery && adduser --system --ingroup delivery delivery
USER delivery

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
