
FROM gradle:8.5-jdk21 AS build


WORKDIR /home/gradle/src

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

COPY src ./src

RUN chmod +x ./gradlew

RUN ./gradlew bootJar --no-daemon


FROM eclipse-temurin:21-jre-jammy


WORKDIR /app


COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]