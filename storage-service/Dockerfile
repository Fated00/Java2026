FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :storage-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/storage-service/build/libs/storage-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
