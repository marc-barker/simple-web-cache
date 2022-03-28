# Smallest base container with JDK17
FROM openjdk:17-alpine
WORKDIR /swc

# Setup user to mitigate several security risks
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Add the built jar file to the container
ARG JAR_FILE=build/libs/simple-web-cache.jar
COPY ${JAR_FILE} swc.jar
COPY docker/docker-application.yml application.yml

# Start the application using the provided classpath
ENTRYPOINT ["java","-jar","swc.jar", "--spring.config.location=file:///swc/application.yml"]
