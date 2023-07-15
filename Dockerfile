FROM openjdk:11-jdk-slim
WORKDIR /app
COPY target/* /app
ENTRYPOINT ["java", "-jar", "react-and-spring-data-rest-0.0.1-SNAPSHOT.jar"]
