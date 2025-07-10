# Plan Deploymentu na GCP

## Kolejność działań:

### 0. Stwórz GKE cluster

```bash
# Stwórz cluster z e2-small
gcloud container clusters create project-chaos-cluster --region=europe-central2 --num-nodes=1 --machine-type=e2-small

# Połącz się z klastrem
gcloud container clusters get-credentials project-chaos-cluster --region=europe-central2
```

### 1. Cloud SQL Setup

```bash
# Stwórz Cloud SQL instance
gcloud sql instances create project-chaos-db --database-version=POSTGRES_14 --tier=db-f1-micro --region=europe-central2 --root-password="$ROOT_PASSWORD"

# Stwórz bazę danych
gcloud sql databases create projectchaos --instance=project-chaos-db

# Stwórz użytkownika aplikacji
gcloud sql users create app-user --instance=project-chaos-db --password="$APP_PASSWORD"

# Pobierz connection name
CONNECTION_NAME=$(gcloud sql instances describe project-chaos-db --format="value(connectionName)")
echo "Connection name: $CONNECTION_NAME"
```

### 2. Dodaj Spring Cloud GCP dependency

```xml
<!-- W pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.cloud</groupId>
            <artifactId>spring-cloud-gcp-dependencies</artifactId>
            <version>4.1.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Cloud SQL Starter dla PostgreSQL -->
    <dependency>
        <groupId>com.google.cloud</groupId>
        <artifactId>spring-cloud-gcp-starter-sql-postgresql</artifactId>
    </dependency>
</dependencies>
```

### 3. Konfiguracja aplikacji

```yaml
# W application.yml lub application.properties
spring:
  cloud:
    gcp:
      sql:
        database-name: projectchaos
        instance-connection-name: ${CONNECTION_NAME}
```

4 - Skonfigurować Oauth w projekcie project-chaos

### 5. Sealed Secrets Setup

```bash
# Dodaj repo
helm repo add sealed-secrets https://bitnami-labs.github.io/sealed-secrets

# Zainstaluj w namespace
helm install sealed-secrets sealed-secrets/sealed-secrets
```

# Pobierz klucz publiczny (do lokalnego kubeseal)

kubectl get secret -l sealedsecrets.bitnami.com/sealed-secrets-key=active -o jsonpath="{.items[0].data['tls\.crt']}" | base64 -d > pub-cert.pem

# Zainstaluj kubeseal lokalnie (do szyfrowania zwykłych sekretów)

### 6. Przygotowanie sekretów (uproszczone)

```bash
# Stwórz sekrety (bez ręcznego JDBC URL!)
kubectl create secret generic project-chaos-secrets --from-literal=SPRING_CLOUD_GCP_SQL_INSTANCE_CONNECTION_NAME="$CONNECTION_NAME" --from-literal=SPRING_CLOUD_GCP_SQL_DATABASE_NAME="projectchaos" --from-literal=SPRING_DATASOURCE_USERNAME="app-user" --from-literal=SPRING_DATASOURCE_PASSWORD="$APP_PASSWORD" --from-literal=OAUTH2_CLIENT_SECRET="your-oauth-secret" --dry-run=client -o yaml secrets.yaml


kubeseal --cert pub-cert.pem --format yaml < secrets.yaml > sealed-secrets.yaml

# Zastosuj
kubectl apply -f sealed-secrets.yaml
```

### 7. Okrojenie Helm charta

- Usunąć niepotrzebne pliki z templates/
- Dostosować values.yaml do aplikacji Java
- Zmienić nazwę z "helm" na "project-chaos"

### 8. Ręczny deployment

```bash
# Test charta
helm template helm/ --values helm/values.yaml

# Zainstaluj charta w namespace
helm install project-chaos helm --values helm/values.yaml
```

### 9. ArgoCD (później)

- Dodać ArgoCD do automatyzacji deploymentu
- Skonfigurować GitOps workflow

## Status:

- [ X ] GKE cluster stworzony
- [ X ] Cloud SQL instance stworzona
- [ X ] Namespace stworzony
- [ X ] Spring Cloud GCP dependency dodane
- [ X ] Sealed Secrets zainstalowane
- [ X ] Sekrety przygotowane i zasealowane
- [ X ] Helm chart okrojony
- [ ] Ręczny deployment udany
- [ ] ArgoCD skonfigurowany
