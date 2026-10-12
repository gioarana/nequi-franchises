FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

COPY src/ src/

RUN ./mvnw -B -DskipTests package \
    && cp target/nequi-franchises-*.jar /workspace/application.jar


FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app app

COPY --from=build --chown=app:app \
    /workspace/application.jar /app/application.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]