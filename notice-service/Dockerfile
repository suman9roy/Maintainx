# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY notice-service/.mvn notice-service/.mvn
COPY notice-service/mvnw notice-service/mvnw
COPY notice-service/pom.xml notice-service/pom.xml

WORKDIR /app/notice-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY notice-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/notice-service/target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java","-jar","app.jar"]
