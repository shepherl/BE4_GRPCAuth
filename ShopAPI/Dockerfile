FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle ./
COPY settings.gradle ./
# Cache dependencies to speed up future builds
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src
# Build the application
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/shopapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
