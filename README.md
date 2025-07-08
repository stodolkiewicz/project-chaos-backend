### To run postgres db:
```
docker run -d --name project-chaos -e POSTGRES_DB=project-chaos -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 postgres:15
```

### Run app locally - dev profile
Add -Dspring.profiles.active=dev as **VM Options**:
![img.png](docs/images/dev-profile.png)

## Current Database Schema
As of 27.04.2025
![Alt text](docs/images/db_schema.png)

tasks.column_id == null ? -> task is in project_backlog

## Docs
### Liquibase
```https://contribute.liquibase.com/extensions-integrations/directory/integration-docs/springboot/configuration/```

## Other
### Test folder structure
```
https://blog.worldline.tech/2020/04/10/split-unit-and-integration-tests.html
```
