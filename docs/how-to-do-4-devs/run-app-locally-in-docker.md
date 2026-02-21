## To build app and run it in docker 
Important: You do not need this for local development. See main README.md

```
docker build -t project-chaos-backend .

docker network create project-chaos-network

docker run -d --name project-chaos-db --network project-chaos-network -e POSTGRES_DB=project-chaos -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 postgres:15

docker run --network project-chaos-network -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev --env-file project-chaos-backend-env.env --env SPRING_DATASOURCE_URL=jdbc:postgresql://project-chaos-db:5432/project-chaos --env SPRING_DATASOURCE_USERNAME=admin --env SPRING_DATASOURCE_PASSWORD=admin123 project-chaos-backend
```

project-chaos-backend-env.env - file with env variables like:

- GOOGLE_OAUTH2_CLIENT_ID
- GOOGLE_OAUTH2_CLIENT_SECRET
- SOCIAL_JWT_SECRET