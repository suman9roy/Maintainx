# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY payment-service/.mvn payment-service/.mvn
COPY payment-service/mvnw payment-service/mvnw
COPY payment-service/pom.xml payment-service/pom.xml

WORKDIR /app/payment-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY payment-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/payment-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar"]
