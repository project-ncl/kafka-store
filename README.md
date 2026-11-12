# kafka store

kafka-store is a Quarkus application for the PNC (Project Newcastle) ecosystem.
It consumes build-duration messages from a Kafka topic, parses them into `BuildStageRecord`
rows in a relational database, and exposes a REST API to query build-stage metrics.

# Build
```bash
mvn clean install
```

# Configuration
See `src/main/resources` for the required information to consume data from kafka

# FAQ

## How to add header files in my new src files?

Run:
```bash
mvn license:format
```
