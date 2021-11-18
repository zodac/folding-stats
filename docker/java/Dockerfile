ARG JAVA_VERSION
ARG MAVEN_VERSION

FROM maven:${MAVEN_VERSION}-openjdk-${JAVA_VERSION} AS builder

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
FROM openjdk:${JAVA_VERSION}-jdk-alpine

COPY --from=builder /folding-stats.jar /folding-stats.jar

ENTRYPOINT ["java","-jar","/folding-stats.jar"]
