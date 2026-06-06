FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :order-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/order-service/build/libs/order-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
