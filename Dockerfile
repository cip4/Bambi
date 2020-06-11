ARG  VERSION=development

# compile and test bambi
FROM openjdk:8-jdk-slim-buster as java-builder

RUN apt-get -qq update >/dev/null \
  && apt-get -qq install git >/dev/null

COPY [".git", "/work/.git"]
COPY ["src", "/work/src"]
COPY ["gradle", "/work/gradle"]
COPY ["build.gradle", "settings.gradle", "gradlew", "/work/"]

WORKDIR /work

RUN ./gradlew -i build -PprojectVersion=${VERSION} --no-daemon

# create final image
FROM openjdk:8-jre-slim-buster

COPY --from=java-builder ["/work/build/libs/*.jar", "/opt/bambi.jar"]

EXPOSE 8080

ENTRYPOINT ["java", "-cp","/opt/bambi.jar", "org.cip4.bambi.server.BambiService"]


