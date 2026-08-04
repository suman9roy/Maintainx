# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY aichat-service/.mvn aichat-service/.mvn
COPY aichat-service/mvnw aichat-service/mvnw
COPY aichat-service/pom.xml aichat-service/pom.xml

WORKDIR /app/aichat-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY aichat-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/aichat-service/target/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","app.jar"]
