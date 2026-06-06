FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/autosalon-backend-lab1-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
