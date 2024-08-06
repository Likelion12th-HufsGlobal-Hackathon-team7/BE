FROM --platform=linux/amd64 openjdk:17-jdk-slim

EXPOSE 8080

ARG JAR_FILE=build/libs/runwithmate-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
