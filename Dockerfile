# ==========================================
# All-in-One Single Container Dockerfile for Codexa
# Bundles React Frontend + Spring Boot Backend into 1 Server
# ==========================================

# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend with Bundled Frontend Assets
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src
COPY --from=frontend-build /app/frontend/dist /app/frontend/dist
COPY --from=frontend-build /app/frontend/dist ./backend/src/main/resources/static
RUN mvn -f ./backend/pom.xml clean package -DskipTests

# Stage 3: Lightweight Runtime Container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "app.jar"]
