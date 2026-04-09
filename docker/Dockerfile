FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Kopiera hela backend-katalogen
COPY backend/ .

# Bygg JAR-filen
RUN mvn clean package -DskipTests

# Runtime-image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Kopiera den byggd JAR-filen från builder
COPY --from=builder /app/target/Lageruthyrning-Examen-0.0.1-SNAPSHOT.jar app.jar

# Exponera port
EXPOSE 8080

# Starta appen
ENTRYPOINT ["java", "-jar", "app.jar"]


