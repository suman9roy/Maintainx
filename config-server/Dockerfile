# syntax=docker/dockerfile:1
# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY config-server/.mvn config-server/.mvn
COPY config-server/mvnw config-server/mvnw
COPY config-server/pom.xml config-server/pom.xml

WORKDIR /app/config-server
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY config-server/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/config-server/target/*.jar config-server.jar

# Clone the config repo at build time using a BuildKit secret.
# The token is mounted only for this RUN and is never written into
# any image layer or build history (unlike `ARG`/`ENV`, which persist
# in `docker history`).
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

RUN --mount=type=secret,id=github_token \
    GITHUB_TOKEN=$(cat /run/secrets/github_token) && \
    git clone --depth 1 --branch master \
      https://${GITHUB_TOKEN}@github.com/suman9roy/maintainx-config-repo.git /config-repo

EXPOSE 8888

ENTRYPOINT ["java","-jar","config-server.jar"]
