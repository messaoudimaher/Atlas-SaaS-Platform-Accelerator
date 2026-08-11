# Stage 1: Build & Layer Extraction
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Gradle wrapper and configuration files
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle .

# Copy shared libraries and target module
COPY shared/ shared/
COPY services/notification-service/ services/notification-service/

# Build Spring Boot executable archive
RUN ./gradlew :services:notification-service:bootJar --no-daemon -x test

# Extract layered components for optimal Docker cache layering
WORKDIR /workspace/extracted
RUN java -Djarmode=layertools -jar /workspace/services/notification-service/build/libs/*.jar extract

# Stage 2: Minimal Production Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create unprivileged system user
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

USER appuser:appgroup

# Copy individual JAR layers
COPY --from=builder /workspace/extracted/dependencies/ ./
COPY --from=builder /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/extracted/application/ ./

EXPOSE 8085

HEALTHCHECK --interval=15s --timeout=5s --retries=3 --start-period=20s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseVirtualThreads", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
