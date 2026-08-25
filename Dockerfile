# ----------- Build stage -----------
FROM maven:3.8.7-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml to download dependencies (improves caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build package
COPY src ./src
RUN mvn clean package -DskipTests

# ----------- Production stage -----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a secure non-root system group and user
RUN addgroup -S hrm && adduser -S hrm -G hrm
USER hrm

# Copy compiled JAR from builder stage using wildcard to support version bumps
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


