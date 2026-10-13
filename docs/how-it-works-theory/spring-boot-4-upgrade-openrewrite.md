## Spring Boot 4 upgrade with OpenRewrite

The command below upgrades the project from Spring Boot 3.5 to 4.0.
It modifies `pom.xml` and the Java sources in place.

```bash
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-spring:RELEASE \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0
```
