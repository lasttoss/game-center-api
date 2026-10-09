# Maven build container 

FROM maven:3.9-eclipse-temurin-17 AS maven_build

WORKDIR /build

COPY pom.xml ./
COPY src ./src

# One build step, not a cached dependency layer: this pom drags in netty's native classifier
# artifacts (netty-tcnative:osx-aarch_64) that are not published to Maven Central, so
# `dependency:go-offline` fails on them even though `package` never needs them. An image builder
# that caches the local repository (BuildKit's cache mount or a warm builder) is the better answer
# for a repeatedly built image.
RUN mvn -B package

#pull base image

FROM eclipse-temurin:17-jre

# The service does not need to write to its own image, and it does not need to be root.
RUN useradd --create-home --uid 10001 app
WORKDIR /app
COPY --from=maven_build /build/target/gamecenter.api-0.0.1-SNAPSHOT.jar /app/app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]