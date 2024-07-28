# CARD COST API
## Overview

Card Cost API is a Spring Boot application designed to provide cost details for various cards. 
This application is built using Java and leverages Docker for easy deployment and management.

## Building the Application

To build the Card Cost API application, you need to run the following command:
`./gradlew docker`

This command will use Gradle to create a Docker image for the application.

## Running the Application

After building the application, you can run it using Docker Compose. Execute the following command:
`docker-compose up`

This will start the application in a Docker container.

## API Documentation

Once the application is running, you can access the API documentation by navigating to the following URL:
`http://localhost:8080/swagger-ui.html`

## Prerequisites

* Docker: Make sure Docker is installed and running on your machine.
* Docker Compose: Ensure Docker Compose is installed to manage multi-container Docker applications.

## Additional Information
* Port Configuration: By default, the application runs on port 8080. Make sure this port is available on your machine 
or modify the Docker configuration if necessary.
* Gradle Wrapper: The `./gradlew` command uses the Gradle Wrapper, which ensures you use the correct version of 
Gradle without needing to install it manually.


