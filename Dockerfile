FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src
RUN mvn clean package assembly:single

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/mypl-jar-with-dependencies.jar ./mypl.jar
ENTRYPOINT ["java", "-jar", "mypl.jar"] 