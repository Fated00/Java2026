FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean test installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/autosalon-backend-lab1/ ./
ENTRYPOINT ["bin/autosalon-backend-lab1"]
