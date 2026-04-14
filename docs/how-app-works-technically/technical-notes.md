# Technical Notes

## Tasks in project backlog
Table tasks has a column_id field. 
If it is empty, it means that the task is in the backlog and should be saved in the project_backlog table.

## Avro schema distribution (JitPack)

Avro schemas are maintained in a separate repository `project-chaos-avro-schemas` and distributed via JitPack.

The version in the schema repo's `pom.xml` does not matter — JitPack uses the **git tag** as the artifact version. To release a new version, create a git tag on the schema repo and update the version in the consumer's dependency declaration accordingly.

```xml
<dependency>
    <groupId>com.github.stodolkiewicz</groupId>
    <artifactId>project-chaos-avro-schemas</artifactId>
    <version>1.0.0</version> <!-- must match a git tag on project-chaos-avro-schemas -->
</dependency>
```
