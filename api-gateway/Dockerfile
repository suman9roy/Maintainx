# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY api-gateway/.mvn api-gateway/.mvn
COPY api-gateway/mvnw api-gateway/mvnw
COPY api-gateway/pom.xml api-gateway/pom.xml

WORKDIR /app/api-gateway
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY api-gateway/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java","-jar","app.jar"]
