# Stage 1: build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /usr/src/app
# As dependencias sao resolvidas antes de copiar o codigo para aproveitar a cache
# de camadas do Docker entre builds em que so o codigo muda.
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy

ENV SERVER_PORT=8090

# Tipos de letra necessarios para a geracao de PDF com iText.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fonts-dejavu fonts-liberation fontconfig curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --create-home --uid 10001 gestao
WORKDIR /usr/app
COPY --from=build /usr/src/app/target/*.jar app.jar
RUN chown -R gestao:gestao /usr/app
USER gestao

EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --retries=5 \
    CMD curl -fsS http://localhost:${SERVER_PORT:-8090}/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "-Djava.awt.headless=true", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
