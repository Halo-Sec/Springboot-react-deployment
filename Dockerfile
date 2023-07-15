FROM openjdk:11-jdk-slim
WORKDIR /app
COPY target/react-and-spring-data-rest-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

