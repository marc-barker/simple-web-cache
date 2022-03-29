# Smallest base container with JDK17
FROM openjdk:17-alpine
WORKDIR /swc

# Add the built jar file to the container
COPY . ./
RUN ./gradlew build

# Start the application using the provided classpath
ENTRYPOINT ["java","-jar","simple-web-cache-server/build/libs/simple-web-cache-server.jar", "--spring.config.location=file:///swc/docker/docker-application.yml"]
