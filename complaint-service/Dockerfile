# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY complaint-service/.mvn complaint-service/.mvn
COPY complaint-service/mvnw complaint-service/mvnw
COPY complaint-service/pom.xml complaint-service/pom.xml

WORKDIR /app/complaint-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY complaint-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/complaint-service/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java","-jar","app.jar"]
