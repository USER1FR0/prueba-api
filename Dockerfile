# ---------- Build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

# ---------- Run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# Render inyecta PORT; la app ya lo lee via server.port=${PORT:8081}
ENTRYPOINT ["java", "-jar", "app.jar"]
