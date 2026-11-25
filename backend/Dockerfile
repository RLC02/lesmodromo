# Etapa 1: compila o projeto com Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: imagem final, só com o Java e o .jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# A pipeline passa o commit aqui; o /health mostra esse valor
ARG APP_VERSAO=local
ENV APP_VERSAO=${APP_VERSAO}

COPY --from=build /app/target/lesmodromo-api.jar app.jar
EXPOSE 8080

# Ajustes de memória para caber nos 512 MB do plano gratuito do Render
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=60", "-Xss512k", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
