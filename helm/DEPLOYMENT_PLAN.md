# Plan Deploymentu na GCP

## Kolejność działań:

### 0. Stwórz GKE cluster

```bash
# Stwórz cluster z e2-small
gcloud container clusters create [gke-cluster-name] --region=europe-central2 --num-nodes=1 --machine-type=e2-small

# Połącz się z klastrem
gcloud container clusters get-credentials [gke-cluster-name] --region=europe-central2
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
- [ X ] Ręczny deployment udany
- [ ] ArgoCD skonfigurowany


Lekcja: Jak dać aplikacji supermoce w Google Cloud

Problem: Twoja aplikacja w domku LEGO (Pod w Kubernetesie) chciała zadzwonić do fabryki klocków Google (bazy danych Cloud SQL), ale system bezpieczeństwa jej nie pozwalał.
Dlaczego? Bo w świecie Google Cloud każdy, kto chce coś zrobić, musi mieć specjalny identyfikator i odpowiednie pozwolenia. To jak dowód osobisty i klucz do odpowiednich drzwi.
Na początku Twoja aplikacja nie miała ani dowodu, ani klucza. Potem miała klucz, ale nie miała dowodu. A na końcu miała i dowód, i klucz, ale ochroniarz budynku (węzeł GKE) nie miał pozwolenia, żeby w ogóle z nią rozmawiać.
Przeszliśmy przez 3 poziomy zabezpieczeń, żeby to naprawić.


Poziom 1: Klucz do drzwi (Rola IAM)
Co zrobiliśmy? Nadaliśmy Twojej aplikacji rolę Klient Cloud SQL.
Czym to jest? To jest jak dać aplikacji klucz do drzwi z napisem "Fabryka Klocków". Bez tego klucza, nawet jakby stała pod drzwiami, to by ich nie otworzyła.
Dlaczego to było potrzebne? Żeby Google wiedziało, że Twoja aplikacja ma pozwolenie na rozmowę z bazą danych.


Poziom 2: Dowód osobisty (Workload Identity)
Nawet z kluczem, system bezpieczeństwa pytał: "A kim Ty w ogóle jesteś, żeby używać tego klucza?". Twoja aplikacja nie miała "dowodu osobistego" Google.
Co to jest Workload Identity? To jest mechanizm, który pozwala stworzyć oficjalny dowód osobisty Google dla Twojej aplikacji w świecie LEGO. To jest super bezpieczne, bo każda aplikacja dostaje swój własny, unikalny dowód.

Co zrobiliśmy?
Stworzyliśmy "prawdziwy" dowód w Google: Google Service Account (GSA) o nazwie project-chaos-sa.
Stworzyliśmy "wewnętrzny" identyfikator w świecie LEGO: Kubernetes Service Account (KSA) o nazwie project-chaos-ksa.
Powiązaliśmy je ze sobą. Powiedzieliśmy Google: "Jeśli ktoś przyjdzie z identyfikatorem project-chaos-ksa, traktuj go tak, jakby miał dowód project-chaos-sa".
Dlaczego to było potrzebne? Żeby Twoja aplikacja mogła się wylegitymować i udowodnić, że ma prawo używać klucza, który jej daliśmy.


Poziom 3: Pozwolenie dla Ochroniarza (Uprawnienia Węzła GKE)
To był nasz ostatni, najtrudniejszy problem. Twoja aplikacja miała już i dowód, i klucz, ale wciąż nie mogła się połączyć. Dlaczego?
Wyobraź sobie, że Twoje domki LEGO stoją na wielkiej, zielonej płycie (to jest Węzeł GKE – maszyna, na której działa aplikacja). Okazało się, że ta płyta miała od początku wbudowaną zasadę: "Nie pozwalam nikomu na moim terenie nawet próbować dzwonić do fabryki klocków".

Co to są "Access Scopes"? To są właśnie te zasady "wypalone" w zielonej płycie. Nasza płyta miała bardzo ograniczone zasady.
Co zrobiliśmy? Nie mogliśmy zmienić zasad starej płyty, więc zrobiliśmy coś sprytniejszego:
Stworzyliśmy nową, lśniącą zieloną płytę (app-pool) z jedną, prostą zasadą: "Można dzwonić do wszystkich fabryk Google" (to jest ten scope o nazwie Cloud Platform).
Przenieśliśmy Twój domek LEGO na tę nową płytę.
Dlaczego to było potrzebne? Żeby "ochroniarz" (węzeł GKE) w ogóle pozwolił Twojej aplikacji (która ma już dowód i klucz) na wykonanie telefonu do bazy danych.