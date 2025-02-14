#!/bin/bash

# Create directories
mkdir -p lib/test

# Download JUnit 5 dependencies
curl -L https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.10.1/junit-jupiter-api-5.10.1.jar -o lib/test/junit-jupiter-api-5.10.1.jar
curl -L https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.10.1/junit-jupiter-engine-5.10.1.jar -o lib/test/junit-jupiter-engine-5.10.1.jar
curl -L https://repo1.maven.org/maven2/org/junit/platform/junit-platform-commons/1.10.1/junit-platform-commons-1.10.1.jar -o lib/test/junit-platform-commons-1.10.1.jar
curl -L https://repo1.maven.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar -o lib/test/opentest4j-1.3.0.jar 