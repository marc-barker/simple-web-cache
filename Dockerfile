# Smallest base container with JDK17
FROM openjdk:17-alpine
WORKDIR /swc

# Build the project (so tests pass) and produce an executable jar file
COPY . ./
CMD ["./gradlew", "build"]

# Run the server with the custom docker configuration (uses docker hostname resolution)
ENTRYPOINT ["java","-jar","simple-web-cache-server/build/libs/simple-web-cache-server.jar", "--spring.config.location=file:///swc/docker/docker-application.yml"]
