FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S sonexa && adduser -S sonexa -G sonexa \
    && apk add --no-cache wget

WORKDIR /app

COPY target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R sonexa:sonexa /app

USER sonexa

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/health/live || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
