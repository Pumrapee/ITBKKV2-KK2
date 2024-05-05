FROM openjdk:17-jdk-alpine
ENV TZ=UTC
COPY target/*.jar /api.jar
ENTRYPOINT ["java","-jar","/api.jar"]