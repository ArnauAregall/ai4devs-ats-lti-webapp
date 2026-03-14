# Stage 1: Build
# Build context must be the repository root:
#   docker build -t lti-ats-backend -f lti-ats-backend/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy all module POMs so Maven can resolve the full reactor
COPY pom.xml ./
COPY lti-ats-backend/pom.xml ./lti-ats-backend/
COPY lti-ats-frontend/pom.xml ./lti-ats-frontend/

# Resolve backend dependencies separately to leverage Docker layer cache
RUN mvn dependency:go-offline -pl lti-ats-backend -am -q

# Copy backend source and build the fat JAR
COPY lti-ats-backend/src ./lti-ats-backend/src
RUN mvn package -pl lti-ats-backend -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/lti-ats-backend/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lti_ats
ENV SPRING_DATASOURCE_USERNAME=lti_ats
ENV SPRING_DATASOURCE_PASSWORD=lti_ats

ENTRYPOINT ["java", "-jar", "app.jar"]
