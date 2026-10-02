# ---------- Build stage ----------
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system offeria \
    && useradd --system \
       --gid offeria \
       --home-dir /app \
       --shell /usr/sbin/nologin \
       offeria

COPY --from=builder --chown=offeria:offeria \
    /app/target/*.jar /app/app.jar

USER offeria

ENV SERVER_PORT=8090
ENV MATERIAL_SERVICE_URL=http://material-service:8081
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8090

HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=3 \
    CMD curl --fail --silent --show-error http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
