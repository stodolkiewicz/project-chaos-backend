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

