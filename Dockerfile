# Stage 1: Run the app
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
# Copy the jar from the gradle build output
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]