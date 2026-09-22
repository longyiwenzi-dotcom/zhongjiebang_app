FROM eclipse-temurin:17-jre
ARG JAR_FILE
WORKDIR /app
COPY ${JAR_FILE} app.jar
RUN useradd --system --uid 10001 appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-XX:+UseG1GC", "-jar", "/app/app.jar"]
