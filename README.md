# Aggregation Service

## ADRs

* [ADR-0001](doc/adr/0001-spring-webflux-usage.md) - Usage of spring webflux framework
* [ADR-0002](doc/adr/0002-trigger-call-for-each-parameter.md) - Trigger a webclient call for each parameter received on request

## Get Started

### Pre-requisites

* JDK 17
* Docker

### Command

```
./mvnw spring-boot:run
```

### Docker Compose support
This project contains a Docker Compose file named `compose.yaml`.

This file is used Spring Boot Docker Compose support and it contains the dependent services in order to run this project.

