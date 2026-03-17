# ══════════════════════════════════════════════════════════════
# Multi-stage Dockerfile — Smart Order AI Backend
# ══════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────
# STAGE 1 — Build
# Uses the official Maven image with Eclipse Temurin 21 JDK.
# The Maven wrapper (mvnw) is skipped — the image provides mvn directly.
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy dependency descriptors first to leverage Docker layer caching.
# If pom.xml hasn't changed, Maven won't re-download the internet.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat jar, skipping tests (they run in CI).
COPY src ./src
RUN mvn package -DskipTests -q

# ─────────────────────────────────────────────
# STAGE 2 — Runtime
# Minimal JRE image — no JDK, no Maven, no build tools in production.
# Distroless runtime keeps the attack surface and CVE count lower.
# ─────────────────────────────────────────────
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

WORKDIR /app

# Copy only the executable jar from the builder stage.
COPY --from=builder /build/target/*.jar /app/app.jar

# Expose the Spring Boot default port.
EXPOSE 8080

# Use exec form to make the JVM the PID 1 (proper signal handling / graceful shutdown).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
