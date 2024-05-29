FROM openjdk:17-jdk-alpine AS build
WORKDIR /workspace/app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine AS run
ENV TZ=UTC
VOLUME /tmp
COPY --from=build /workspace/app/target /code

ENTRYPOINT ["java", "-jar", "code/kanbanapi-0.0.1-SNAPSHOT.jar"]
