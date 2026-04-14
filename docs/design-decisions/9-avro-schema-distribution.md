# ADR 009: Avro Schema Distribution Strategy

## Status

Accepted

---

## Context

The application uses Apache Kafka to publish `VectorizationMessage` events when an attachment is uploaded. A separate consumer service will process these messages and trigger vectorization.

Kafka messages are serialized using **Apache Avro** with **Confluent Schema Registry** (`localhost:8081` in dev, deployed separately in prod). Avro requires both the producer and the consumer to share the same schema definition.

Schemas are defined as `.avsc` files and compiled to Java classes via `avro-maven-plugin`. The question is: how should these schema files and generated classes be shared between services?

### Constraints

- Two services involved: `project-chaos-backend` (producer) and a future consumer service
- Schema artifact is not yet published to a remote registry — local install used during development
- Schema Registry handles runtime compatibility validation
- Project is in early development; operational overhead should be minimal
- Repository is public on GitHub

---

## Problem

Avro schemas must be consistent between producer and consumer. There are several ways to achieve this:

1. Duplicate `.avsc` files in each repo — fragile, schema drift risk
2. Shared Maven artifact published to a remote registry — requires CI/CD or manual publish steps
3. Shared Maven artifact installed locally — simple, no infrastructure needed
4. `GenericRecord` deserialization on the consumer — no shared code, but no type safety

---

## Decision

Maintain Avro schemas in a dedicated repository: **`project-chaos-avro-schemas`**.

The artifact is resolved via **JitPack** — both locally and in CI/CD pipelines. Zero configuration required on the schema repo side. JitPack builds the artifact on demand from the public GitHub repository, referenced by git tag.

Consumer dependency declaration:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.stodolkiewicz</groupId>
    <artifactId>project-chaos-avro-schemas</artifactId>
    <version>1.0.0</version> <!-- git tag -->
</dependency>
```

The `project-chaos-avro-schemas` repo contains:
- `.avsc` schema files under `src/main/avro/`
- `avro-maven-plugin` configured to generate Java classes at `generate-sources` phase
- A standard `pom.xml` with `groupId: com.stodo`

Schema Registry remains the runtime source of truth for compatibility enforcement.

---

## Failure Analysis

| Scenario | Result |
|---|---|
| Consumer uses outdated local artifact | Compile-time mismatch caught before runtime |
| Schema changes without `mvn install` locally | Build fails — explicit signal to update |
| Schema Registry down in dev | Producer fails to send — expected, SR is required |
| Breaking schema change pushed to avro-schemas repo | Schema Registry rejects registration if compatibility mode is set |
| JitPack unavailable during CI build | Build fails — JitPack is an external dependency |

---

## Accepted Risks

### JitPack is an external service dependency

CI/CD pipelines depend on JitPack availability. JitPack builds on demand — the first build after a new tag may be slow. Accepted — the project is public and JitPack has proven reliability for open source projects. Migrate to GitHub Packages if availability becomes a concern.

---

## Alternatives Considered

### GitHub Packages

Publish the artifact to GitHub Packages via `mvn deploy`. Requires a GitHub token configured in both the schema repo pipeline (`mvn deploy`) and every consuming repo pipeline (`mvn package`). More control than JitPack but significantly more setup. Rejected in favour of JitPack for now — revisit if the repo becomes private.

### `GenericRecord` deserialization

Consumer deserializes messages without a shared schema artifact, accessing fields by name at runtime. Eliminates the shared artifact problem but removes compile-time type safety. Rejected — the safety guarantees of `SpecificRecord` outweigh the convenience.

### Monorepo / Maven multi-module

Add `project-chaos-avro-schemas` as a submodule or Maven module within `project-chaos-backend`. Simplifies local setup but couples the schema lifecycle to the backend release cycle. Rejected — schemas should be independently versioned and shared across services.

---

## Consequences

**Pros:**
- Single source of truth for all Avro schemas
- Full compile-time type safety on both producer and consumer
- No configuration required on schema repo for CI/CD — JitPack handles it
- No tokens or credentials needed in pipelines
- Schema Registry enforces runtime compatibility independently

**Cons:**
- CI/CD depends on JitPack availability (external service)
- First JitPack build after a new tag is slow (built on demand)

---

## Summary

Avro schemas are maintained in a dedicated `project-chaos-avro-schemas` repository. The artifact is resolved through JitPack both locally and in CI/CD pipelines — no additional configuration on the schema repo side required. This approach provides type-safe schema sharing with minimal operational overhead for a public GitHub project.
