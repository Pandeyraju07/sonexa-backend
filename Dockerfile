FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /src
RUN apk add --no-cache bash curl unzip
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S sonexa && adduser -S sonexa -G sonexa \
    && apk add --no-cache wget
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R sonexa:sonexa /app
USER sonexa
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=cloud
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD sh -c "wget -qO- http://127.0.0.1:${PORT:-8080}/api/v1/health/live || exit 1"
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

