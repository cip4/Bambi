ARG VERSION=dev
ARG BUILD_NUMBER=na
ARG GIT_REV=na

# compile and test bambi
FROM amazoncorretto:8-alpine-jdk as java-builder

ARG VERSION
ARG BUILD_NUMBER
ARG GIT_REV

COPY [".git", "/work/.git"]
COPY ["src", "/work/src"]
COPY ["gradle", "/work/gradle"]
COPY ["build.gradle", "settings.gradle", "gradlew", "/work/"]

RUN apk add --no-cache dos2unix

WORKDIR /work

RUN dos2unix gradlew
RUN ./gradlew -i build -PprojectVersion=${VERSION} -PbuildNumber=${BUILD_NUMBER} --no-daemon >/dev/null

# create final image
FROM amazoncorretto:8-alpine-jre

ARG VERSION
ARG BUILD_NUMBER
ARG GIT_REV

ENV VERSION=${VERSION}
ENV BUILD_NUMBER=${BUILD_NUMBER}
ENV GIT_REV=${GIT_REV}

RUN addgroup -S cip4 && adduser -S cip4 -G cip4 && \
    mkdir /bambidata && chown cip4:cip4 /bambidata && \
    mkdir /BambiHF && chown cip4:cip4 /BambiHF

COPY --chown=cip4:cip4 --from=java-builder ["/work/build/libs/*.jar", "/app/bambi.jar"]

USER cip4

EXPOSE 8080

ENTRYPOINT ["java", "-cp","/app/bambi.jar", "org.cip4.bambi.server.BambiService"]


