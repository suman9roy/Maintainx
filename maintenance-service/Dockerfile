# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY maintenance-service/.mvn maintenance-service/.mvn
COPY maintenance-service/mvnw maintenance-service/mvnw
COPY maintenance-service/pom.xml maintenance-service/pom.xml

WORKDIR /app/maintenance-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY maintenance-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/maintenance-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
