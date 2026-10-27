# FineX backend – Docker image (a gyökérben lévő docker-compose.yml használja)

# Fordítás: a függőségek külön rétegben, a tesztek a fejlesztői gépen futnak (Testcontainers)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Futtatás: csak JRE, nem root felhasználóként
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S finex && adduser -S finex -G finex
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER finex
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
