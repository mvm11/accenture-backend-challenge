FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test -x validateStructure

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]