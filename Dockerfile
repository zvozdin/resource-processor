########Maven build stage########
FROM maven:3.8.1-openjdk-17-slim as maven_build

MAINTAINER Oleksandr Zvozdin <zvyozdin@gmail.com>

WORKDIR /project
COPY pom.xml .
COPY src ./src

# build the app and download dependencies only when these are new (thanks to the cache)
RUN --mount=type=cache,target=/root/.m2  mvn clean package -Dmaven.test.skip

########JRE run stage########
FROM eclipse-temurin:17-jre-alpine

WORKDIR /application

COPY --from=maven_build /project/target/*.jar /application/app.jar

ENTRYPOINT ["java","-jar","/application/app.jar"]
