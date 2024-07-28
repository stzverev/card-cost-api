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

## Cache configuration 

To reduce the number of requests to thirdparty provider, this application causes IIN information.
Cache can be configured by the following environment variables in docker-compose file:
* APP_IINCACHE_TIMEUNIT: Specifies the time unit for cache expiration. Possible values:
    * milliseconds
    * seconds
    * minutes
    * days
    * hours
* APP_IINCACHE_PERIOD: Sets the cache duration based on the specified time unit. Type: Long
* APP_IINCACHE_ENABLED: Enables or disables caching. Possible values:
  * true
  * false

## Prerequisites

* Docker: Make sure Docker is installed and running on your machine.
* Docker Compose: Ensure Docker Compose is installed to manage multi-container Docker applications.

## Additional Information
* Port Configuration: By default, the application runs on port 8080. Make sure this port is available on your machine 
or modify the Docker configuration if necessary.
* Gradle Wrapper: The `./gradlew` command uses the Gradle Wrapper, which ensures you use the correct version of 
Gradle without needing to install it manually.


