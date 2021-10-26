FROM maven:3.8.2-openjdk-17 AS builder

# Build folding-stats
RUN mkdir --parents /folding-stats/

# Copy source code
# Adding and building each module incrementally so we can retain some docker caches when building for tests
COPY ./pom.xml /folding-stats/pom.xml
RUN cd /folding-stats/ && mvn install -Pdocker && cd /

COPY ./folding-stats-api/pom.xml /folding-stats/folding-stats-api/pom.xml
COPY ./folding-stats-api/src/main /folding-stats/folding-stats-api/src/main
RUN cd /folding-stats/folding-stats-api && mvn install --projects . && cd /

COPY ./folding-stats-client-library-java/pom.xml /folding-stats/folding-stats-client-library-java/pom.xml
COPY ./folding-stats-client-library-java/src/main /folding-stats/folding-stats-client-library-java/src/main
RUN cd /folding-stats/folding-stats-client-library-java && mvn install --projects . && cd /

COPY ./folding-stats-jar/pom.xml /folding-stats/folding-stats-jar/pom.xml
COPY ./folding-stats-jar/src/main /folding-stats/folding-stats-jar/src/main
RUN cd /folding-stats/folding-stats-jar && mvn install --projects . && cd /

COPY ./folding-stats-war/pom.xml /folding-stats/folding-stats-war/pom.xml
COPY ./folding-stats-war/src/main /folding-stats/folding-stats-war/src/main
RUN cd /folding-stats/folding-stats-war && mvn install --projects . && cd /

COPY ./stubbed-folding-endpoints-war/pom.xml /folding-stats/stubbed-folding-endpoints-war/pom.xml
COPY ./stubbed-folding-endpoints-war/src/main /folding-stats/stubbed-folding-endpoints-war/src/main
RUN cd /folding-stats/stubbed-folding-endpoints-war && mvn install --projects . && cd /

# Build and output final artifacts
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/ && \
    mv /folding-stats/folding-stats-war/target/folding-stats-war*.war /opt/jboss/wildfly/standalone/deployments/folding-stats-war.war && \
    mv /folding-stats/stubbed-folding-endpoints-war/target/stubbed-folding-endpoints-war*.war /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war && \
    rm -rf /folding-stats/ "${MAVEN_CONFIG}" "${MAVEN_HOME}"

FROM jboss/wildfly:24.0.0.Final

# Copy the WAR artifacts
COPY --from=builder /opt/jboss/wildfly/standalone/deployments/ /opt/jboss/wildfly/standalone/deployments/

# Remove the stubbed endpoints if this is not a test deployment
ARG DEPLOYMENT_TYPE
RUN if [ "${DEPLOYMENT_TYPE}" != "test" ]; then \
       rm -rf /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war; \
    fi

# Configuration files based off Wildfly 24.0.0.Final, may require updates as Wildfly version changes
COPY ./docker/wildfly/config/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
COPY ./docker/wildfly/config/standalone.conf /opt/jboss/wildfly/bin/standalone.conf

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD
RUN /opt/jboss/wildfly/bin/add-user.sh "${ADMIN_USERNAME}" "${ADMIN_PASSWORD}" --silent;
USER "${ADMIN_USERNAME}"

# Set default directory to the log directory
WORKDIR /opt/jboss/wildfly/standalone/log
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
