FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ai-interview-common/pom.xml ai-interview-common/
COPY ai-interview-dao/pom.xml ai-interview-dao/
COPY ai-interview-service/pom.xml ai-interview-service/
COPY ai-interview-web/pom.xml ai-interview-web/
COPY ai-interview-api/pom.xml ai-interview-api/
RUN mvn dependency:go-offline -B

COPY . .
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/ai-interview-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
