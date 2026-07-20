# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY auth-service/.mvn auth-service/.mvn
COPY auth-service/mvnw auth-service/mvnw
COPY auth-service/pom.xml auth-service/pom.xml

WORKDIR /app/auth-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY auth-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
