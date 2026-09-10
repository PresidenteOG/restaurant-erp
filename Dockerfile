# syntax=docker/dockerfile:1

# ---- Build ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q clean package -DskipTests

# ---- Run ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/restaurantus-erp-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=demo
ENTRYPOINT ["java", "-jar", "app.jar"]
