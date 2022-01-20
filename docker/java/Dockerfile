ARG JAVA_VERSION
ARG MAVEN_VERSION
ARG DEBIAN_VERSION

FROM maven:${MAVEN_VERSION}-openjdk-${JAVA_VERSION} AS maven_builder

# Build folding-stats
RUN mkdir --parents /folding-stats-spring-boot

WORKDIR /folding-stats-spring-boot

ARG NEXUS_URL
COPY ./docker/java/scripts/configure_custom_nexus.sh /tmp/configure_custom_nexus.sh
RUN /tmp/configure_custom_nexus.sh "${NEXUS_URL}"

# Copy source code
# Adding and building each module incrementally so we can retain some docker caches when building for tests
COPY ./pom.xml ./pom.xml
RUN mvn install -Pdocker

COPY ./folding-stats-api/pom.xml ./folding-stats-api/pom.xml
COPY ./folding-stats-api/src/main ./folding-stats-api/src/main
RUN cd ./folding-stats-api && mvn install --projects . && cd ..

COPY ./folding-stats-client-library-java/pom.xml ./folding-stats-client-library-java/pom.xml
COPY ./folding-stats-client-library-java/src/main ./folding-stats-client-library-java/src/main
RUN cd ./folding-stats-client-library-java && mvn install --projects . && cd ..

COPY ./folding-stats-jar/pom.xml ./folding-stats-jar/pom.xml
COPY ./folding-stats-jar/src/main ./folding-stats-jar/src/main
RUN cd ./folding-stats-jar && mvn install --projects . && cd ..

COPY ./folding-stats-rest/pom.xml ./folding-stats-rest/pom.xml
COPY ./folding-stats-rest/src/main ./folding-stats-rest/src/main
RUN cd ./folding-stats-rest && mvn install --projects . && cd ..

RUN cd ./folding-stats-rest && mvn package
RUN mv ./folding-stats-rest/target/folding-stats-rest*.jar /folding-stats.jar

ARG JAVA_VERSION
FROM openjdk:${JAVA_VERSION}-jdk-slim AS jdk_builder

RUN apt-get update && apt-get install -y binutils

RUN jlink --compress=2 \
    --no-header-files \
    --no-man-pages \
    --strip-debug \
    --add-modules \
java.base,\
java.compiler,\
java.datatransfer,\
java.desktop,\
java.instrument,\
java.logging,\
java.management,\
java.management.rmi,\
java.naming,\
java.net.http,\
java.prefs,\
java.rmi,\
java.scripting,\
java.se,\
java.security.jgss,\
java.security.sasl,\
java.smartcardio,\
java.sql,\
java.sql.rowset,\
java.transaction.xa,\
java.xml,\
java.xml.crypto,\
jdk.accessibility,\
jdk.attach,\
jdk.charsets,\
jdk.compiler,\
jdk.crypto.cryptoki,\
jdk.crypto.ec,\
jdk.dynalink,\
jdk.editpad,\
jdk.hotspot.agent,\
jdk.httpserver,\
jdk.incubator.foreign,\
jdk.incubator.vector,\
jdk.internal.ed,\
jdk.internal.jvmstat,\
jdk.internal.le,\
jdk.internal.opt,\
jdk.internal.vm.ci,\
jdk.jartool,\
jdk.javadoc,\
jdk.jcmd,\
jdk.jconsole,\
jdk.jdeps,\
jdk.jdi,\
jdk.jdwp.agent,\
jdk.jfr,\
jdk.jlink,\
jdk.jpackage,\
jdk.jshell,\
jdk.jsobject,\
jdk.jstatd,\
jdk.localedata,\
jdk.management,\
jdk.management.agent,\
jdk.management.jfr,\
jdk.naming.dns,\
jdk.naming.rmi,\
jdk.net,\
jdk.nio.mapmode,\
jdk.sctp,\
jdk.security.auth,\
jdk.security.jgss,\
jdk.unsupported,\
jdk.unsupported.desktop,\
jdk.xml.dom,\
jdk.zipfs \
    --output /opt/jdk

RUN strip -p --strip-unneeded /opt/jdk/lib/server/libjvm.so

FROM debian:${DEBIAN_VERSION}-slim AS spring_base

COPY --from=jdk_builder /opt/jdk /opt/jdk
COPY --from=maven_builder /folding-stats.jar /folding-stats.jar
COPY ./docker/java/scripts/startup.sh /startup.sh
COPY ./docker/java/config/logback.xml /var/backend/logs/logback.xml

ENV JAVA_HOME "/opt/jdk"
ENV PATH "${PATH}:${JAVA_HOME}/bin"

# Set default directory to the log directory
WORKDIR /var/backend/logs/

# Install nano to allow user to connect to container and edit logback.xml
RUN apt-get update && \
    apt-get install -y \
      nano \
    && \
    apt-get autoremove && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -g 999 backend_user && \
    useradd --create-home --system --uid 999 --gid backend_user backend_user && \
    chown -R backend_user /var/backend/logs/ /startup.sh
USER backend_user

ENTRYPOINT [ "/startup.sh" ]
