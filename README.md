## To run postgres db (do this for local development):
```
docker run -d --name project-chaos -e POSTGRES_DB=project-chaos -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 postgres:15
```

## Run app locally - dev profile
Add -Dspring.profiles.active=dev as **VM Options**:
![img.png](docs/images/dev-profile.png)

# Current Database Schema
As of 27.04.2025
![Alt text](docs/images/db_schema.png)

tasks.column_id == null ? -> task is in project_backlog

# Docs
## Liquibase
```https://contribute.liquibase.com/extensions-integrations/directory/integration-docs/springboot/configuration/```

# Other
## Test folder structure
```
https://blog.worldline.tech/2020/04/10/split-unit-and-integration-tests.html
```

## To build app and run it in docker (do this to test that container is built correctly):
```
docker build -t project-chaos-backend .

docker network create project-chaos-network

docker run -d --name project-chaos-db --network project-chaos-network -e POSTGRES_DB=project-chaos -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 postgres:15

docker run --network project-chaos-network -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev --env-file project-chaos-backend-env.env --env SPRING_DATASOURCE_URL=jdbc:postgresql://project-chaos-db:5432/project-chaos --env SPRING_DATASOURCE_USERNAME=admin --env SPRING_DATASOURCE_PASSWORD=admin123 project-chaos-backend
```
project-chaos-backend-env.env  - file with env variables like:
- GOOGLE_OAUTH2_CLIENT_ID
- GOOGLE_OAUTH2_CLIENT_SECRET
- SOCIAL_JWT_SECRET  