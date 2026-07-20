# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY expense-service/.mvn expense-service/.mvn
COPY expense-service/mvnw expense-service/mvnw
COPY expense-service/pom.xml expense-service/pom.xml

WORKDIR /app/expense-service
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY expense-service/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/expense-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","app.jar"]
