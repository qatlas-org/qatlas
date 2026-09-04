# Build context must be the REPO ROOT (not backend/), since this is a
# multi-module Maven project and needs the parent pom to resolve.
# Build with:  docker build -f backend/Dockerfile -t qatlas/backend:1.0.0 .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy just the poms first so Docker can cache the dependency download layer
# separately from source changes (faster rebuilds).
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -pl backend -am dependency:go-offline -q || true

COPY backend/src backend/src
RUN mvn -B -pl backend -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Matches application-container.yml: app.data.root-path=/app/data
RUN mkdir -p /app/data

COPY --from=build /workspace/backend/target/qatlas-backend-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=container -jar app.jar"]
