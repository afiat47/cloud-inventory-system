FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /src

COPY api/pom.xml api/pom.xml

RUN mvn -f api/pom.xml -B dependency:go-offline

COPY api/src api/src

RUN mvn -f api/pom.xml -B clean package -DskipTests

FROM eclipse-temurin:21-jre-noble AS final

WORKDIR /app

RUN adduser --disabled-password --gecos "" appuser

COPY --from=build /src/api/target/*.jar /app/app.jar

RUN chown -R appuser:appuser /app

USER appuser

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
