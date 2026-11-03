# AGENTS.md

This file provides guidance to coding agents (Claude Code, and other AGENTS.md-aware tools) when working with code in this repository.

## Overview

kafka-store is a Quarkus (JVM, Java 17) application for the PNC (Project Newcastle) ecosystem.
It consumes build-duration messages from a Kafka topic, parses them into `BuildStageRecord`
rows in a relational database, and exposes a REST API to query build-stage metrics.

## Commands

There is no Maven wrapper (`mvnw`); use a locally installed `mvn`.

- Build: `mvn clean install -DskipTests=true`
- Run tests: `mvn test`
- Run a single test class: `mvn test -Dtest=BuildMetricsFetcherTest`
- Run a single test method: `mvn test -Dtest=BuildMetricsFetcherTest#methodName`
- Build + push container image (Jib): `mvn package -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true`
- Apply license headers to new source files: `mvn license:format` (headers are also auto-applied
  during the `compile` phase).
- Apply code formatting: `mvn spotless:apply`

CI (GitHub Actions) delegates to shared reusable workflows in `project-ncl/shared-github-actions`
(build on PR, Mend SCA/SAST scans, releases). Releases and version bumps are triggered manually
via `workflow_dispatch` jobs, not by normal pushes.

## Architecture

The data flow is a one-way pipeline: **Kafka → Consumer → Mapper → Entity → DB**, with a separate
**REST → Fetcher → Entity → DTO** read path.

### Generated code

`Constants.java` is **generated** at build time (`process-sources`) by `maven-replacer-plugin` from
`src/main/templates/org/jboss/pnc/kafkastore/Constants.java`. Edit the template, not the generated
`src/main/java/org/jboss/pnc/kafkastore/Constants.java`. It injects project version, git commit id,
and build timestamp.

## Configuration & environments

Config is in `src/main/resources/application.properties`.

Manual SQL migration scripts live in `src/main/resources/db-update/` (applied out-of-band; Hibernate
only does `update`/`drop-and-create`, it does not run these).

## Conventions

- Lombok is used throughout
- Observability: methods are annotated with Micrometer `@Timed` and OpenTelemetry `@WithSpan` /
  `@SpanAttribute`; keep that instrumentation when adding comparable methods.
- Tests use `@QuarkusTest` with `H2DatabaseTestResource`; AssertJ for assertions, Podam for test data,
  RestAssured for endpoint tests. Note `MainRestTest` is currently `@Disabled` (marked for rewrite).
- License headers are mandatory on all source files (enforced by the license plugin).

## Safety

- Never commit hardcoded API keys or secret tokens.
