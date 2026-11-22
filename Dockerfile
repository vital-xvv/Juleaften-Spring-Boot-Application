# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package \
 && rm -f target/*.jar.original

FROM eclipse-temurin:25-jre-alpine AS extract
WORKDIR /builder
COPY --from=build /src/target/juleaften-1.0.4.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-alpine
WORKDIR /application

RUN addgroup -S spring && adduser -S spring -G spring \
 && apk add --no-cache wget

COPY --from=extract --chown=spring:spring /builder/extracted/dependencies/ ./
COPY --from=extract --chown=spring:spring /builder/extracted/spring-boot-loader/ ./
COPY --from=extract --chown=spring:spring /builder/extracted/snapshot-dependencies/ ./
COPY --from=extract --chown=spring:spring /builder/extracted/application/ ./

USER spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "application.jar"]
