FROM eclipse-temurin:17-jdk-alpine

MAINTAINER Oleksandr Zvozdin <zvyozdin@gmail.com>

ARG JAR_FILE=./target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
