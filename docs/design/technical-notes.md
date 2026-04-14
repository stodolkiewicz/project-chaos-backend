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

# How to push locally built image to GCP artifact registry

1. Log in  
   gcloud auth login


2. Allow docker to push to given region on GCP  
   gcloud auth configure-docker [artifact-registry-region]-docker.pkg.dev


3. Build image locally  
   docker build -t project-chaos-backend .


4. Tag image  
   docker tag project-chaos-backend:latest [artifact-registry-region]-docker.pkg.dev/[GCP-project-id]/[artifact-registry-name]/project-chaos-backend:1.0.0


5. Push  
   docker push [artifact-registry-region]-docker.pkg.dev/[GCP-project-id]/[artifact-registry-name]/project-chaos-backend:1.0.0