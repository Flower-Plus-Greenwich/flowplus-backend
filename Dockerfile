# Multi-stage build for Spring Boot FlowerPlus Backend
# Stage 1: Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execution permission for mvnw
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Application environment configuration
# These variables should be configured in Render or other hosting services
ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8080

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
