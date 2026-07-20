# ============================
# Build Stage
# ============================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Root parent POM + this service module files
COPY pom.xml .
COPY discovery-server/.mvn discovery-server/.mvn
COPY discovery-server/mvnw discovery-server/mvnw
COPY discovery-server/pom.xml discovery-server/pom.xml

WORKDIR /app/discovery-server
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY discovery-server/src src

RUN ./mvnw clean package -DskipTests

# ============================
# Runtime Stage
# ============================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/discovery-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","app.jar"]
