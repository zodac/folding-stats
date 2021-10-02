FROM maven:3.8.2-openjdk-17 AS maven

# Configure maven
COPY --from=maven /usr/share/maven /usr/share/maven
ARG MAVEN_HOME=/usr/share/maven
ARG MAVEN_CONFIG="${USER_HOME_DIR}/.m2"
ARG PATH=${PATH}:${MAVEN_HOME}

# Build folding-stats
RUN mkdir --parents /folding-stats/

# Copy source code
COPY ./pom.xml /folding-stats/pom.xml
COPY ./folding-stats-api /folding-stats/folding-stats-api
COPY ./folding-stats-client-library-java /folding-stats/folding-stats-client-library-java
COPY ./folding-stats-jar /folding-stats/folding-stats-jar
COPY ./folding-stats-war /folding-stats/folding-stats-war
COPY ./stubbed-folding-endpoints-war /folding-stats/stubbed-folding-endpoints-war
# Unused poms must also be added, or else 'mvn install' will not even start
COPY ./testsuite/pom.xml /folding-stats/testsuite/pom.xml

# Build and output artifacts
RUN cd  /folding-stats/ && \
    mvn install && \
    cd / && \
    mkdir --parents /opt/jboss/wildfly/standalone/deployments/ && \
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

# Only add the stubbed endpoints if this is a test deployment
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