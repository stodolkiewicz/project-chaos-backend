### To run postgres db:
```
docker run -d --name social -e POSTGRES_DB=social -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 postgres:15
```