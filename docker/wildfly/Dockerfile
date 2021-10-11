FROM maven:3.8.2-openjdk-17 AS maven

# Configure maven
COPY --from=maven /usr/share/maven /usr/share/maven
ARG MAVEN_HOME=/usr/share/maven
ARG MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
ARG PATH=${PATH}:${MAVEN_HOME}

# Build folding-stats
RUN mkdir --parents /folding-stats/

# Copy source code
# Adding and building each module incrementally so we can retain some docker caches when building for tests
COPY ./pom.xml /folding-stats/pom.xml
RUN cd /folding-stats/ && mvn install -Pdocker && cd /

COPY ./folding-stats-api /folding-stats/folding-stats-api
RUN cd /folding-stats/folding-stats-api && mvn install --projects . && cd /

COPY ./folding-stats-client-library-java /folding-stats/folding-stats-client-library-java
RUN cd /folding-stats/folding-stats-client-library-java && mvn install --projects . && cd /

COPY ./folding-stats-jar /folding-stats/folding-stats-jar
RUN cd /folding-stats/folding-stats-jar && mvn install --projects . && cd /

COPY ./folding-stats-war /folding-stats/folding-stats-war
RUN cd /folding-stats/folding-stats-war && mvn install --projects . && cd /

COPY ./stubbed-folding-endpoints-war /folding-stats/stubbed-folding-endpoints-war
RUN cd /folding-stats/stubbed-folding-endpoints-war && mvn install --projects . && cd /

# Build and output final artifacts
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/ && \
    mv /folding-stats/folding-stats-war/target/folding-stats-war*.war /opt/jboss/wildfly/standalone/deployments/folding-stats-war.war && \
    mv /folding-stats/stubbed-folding-endpoints-war/target/stubbed-folding-endpoints-war*.war /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war && \
    rm -rf /folding-stats/ "${MAVEN_CONFIG}" "${MAVEN_HOME}"

FROM jboss/wildfly:24.0.0.Final AS wildfly

# Configure JDK
COPY --from=folding-stats_jdk /opt/jdk /opt/jdk
ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:/opt/jdk/bin
ENV LANG C.UTF-8

# Configure artifacts
COPY --from=maven /opt/jboss/wildfly/standalone/deployments/ /opt/jboss/wildfly/standalone/deployments/

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