FROM node:22-alpine AS frontend-build
WORKDIR /app
COPY flora-fatalis-frontend/package*.json ./
RUN npm ci
COPY flora-fatalis-frontend/ ./
RUN npx ng build --configuration production

FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app
COPY flora-fatalis-backend/ ./
COPY --from=frontend-build /app/dist/flora-fatalis-frontend/browser src/main/resources/static
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
