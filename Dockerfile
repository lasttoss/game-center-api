# Maven build container 

FROM maven:3.6.3-openjdk-17 AS maven_build

COPY pom.xml /tmp/

COPY src /tmp/src/

WORKDIR /tmp/

RUN mvn package

#pull base image

FROM eclipse-temurin:17

#expose port 8080
#EXPOSE 8088

#default command
CMD java -jar /data/gamecenter.api-0.0.1-SNAPSHOT.jar

#copy hello world to docker image from builder image
COPY --from=maven_build /tmp/target/gamecenter.api-0.0.1-SNAPSHOT.jar /data/gamecenter.api-0.0.1-SNAPSHOT.jar