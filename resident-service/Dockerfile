# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY resident-service/.mvn resident-service/.mvn
COPY resident-service/mvnw resident-service/mvnw
COPY resident-service/pom.xml resident-service/pom.xml

WORKDIR /app/resident-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY resident-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/resident-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
