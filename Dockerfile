ARG VERSION=dev
ARG BUILD_NUMBER=n.a.
ARG GIT_REV=n.a.

# compile and test bambi
FROM amazoncorretto:11-alpine-jdk as java-builder

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
RUN ./gradlew -i startScriptsHeadless installDist -PprojectVersion=${VERSION} -PbuildNumber=${BUILD_NUMBER} --no-daemon >/dev/null

# create final image
FROM amazoncorretto:11-alpine

ARG VERSION
ARG BUILD_NUMBER
ARG GIT_REV

ENV VERSION=${VERSION}
ENV BUILD_NUMBER=${BUILD_NUMBER}
ENV GIT_REV=${GIT_REV}

ENV CIP4_BAMBI_BASE_URL=http://localhost:8080

RUN addgroup -S cip4 && adduser -S cip4 -G cip4 && \
    mkdir /bambidata && chown cip4:cip4 /bambidata && \
    mkdir /BambiHF && chown cip4:cip4 /BambiHF

COPY --chown=cip4:cip4 --from=java-builder ["/work/build/install/CIP4 Bambi", "/app"]

USER cip4

EXPOSE 8080

ENTRYPOINT ["/app/bin/bambi-headless"]


