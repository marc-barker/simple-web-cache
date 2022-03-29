# Simple Web Cache (SWC)

## What is SWC?
___

simple-web-cache is a service that executes simple http requests (currently only GET) and stores the returned content in a file storage system. SWC stores a mapping from url to file storage location, enabling cached retrieval of the content. This can be useful for things like iterating on web scrapers, as it allows you iterate on parsing logic without making multiple network requests. In future, it could be used for offline development where you need to cache website contents for future work where an internet connection is not available.

Currently, the only mapping implementation is transient (i.e. only lasts for lifetime of the server JVM), so all file mappings are lost on server restart. The intended solution for this is to add a simple Redis integration for persistence of the mappings. 

## Getting started
___

To start a local server with default file storage (SeaweedFS) running in Docker, run the following commands in the project root directory:

`./gradlew build`

`docker-compose up -d`

SWC is a simple Java service with one main public endpoint: `getUrl`. This endpoint accepts POST requests with a payload containing a URL (String), and returns the binary content of the response with the associated mime type included in the response header.

An example request to a locally running docker container to retrieve the contents of `www.google.com` would be:

`curl --location --request POST 'http://localhost:8081/getUrl' \
--header 'Content-Type: application/json' \
--data-raw '{
"url": "https://www.google.com"
}'`

## Architecture
___

SWC is written in Java, and makes use of the following tool chain:
- Conjure for API definitions and serde class generation (see `simple-web-cache-api`).
- spring-boot for the web server.
- SeaweedFS for file storage backend.
- Gradle for build system.
- Junit and AssertJ for testing.
- Docker for containerisation and local deployment.

Basic locking has been implemented to prevent concurrent requests from storing the same file twice, see `ReadWriteLockCache.java`.

## Outstanding/known issues

- There is an error when writing the lock file for `gradle-consistent-versions`, but this appears benign (the lock file is written correctly and builds work).
- Support for a persistent key-value store needs to be added, most likely Redis.
- The URL request code and api needs to be broken up and renamed to be more consistent with http standards, and potentially to support other protocols.
- API documentation needs to be added and published.
- Docker integration tests should be added to reduce e2e testing overhead.
