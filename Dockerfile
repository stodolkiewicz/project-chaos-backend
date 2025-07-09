### STAGE 1: BUILDER ###
FROM eclipse-temurin:21.0.2_13-jdk-jammy AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean install

# docker build -t project-chaos-backend .                             => will return this image (final)
# docker build -t project-chaos-backend --target [builder/final] .    => will return the chosen image builder or final
### STAGE 2: FINAL ###
FROM eclipse-temurin:21.0.2_13-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]
# https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/

