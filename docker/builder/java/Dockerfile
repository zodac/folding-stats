FROM openjdk:17-jdk-slim AS jdk

RUN apt-get update && \
    apt-get install --yes binutils && \
    apt-get clean all && \
    jlink \
        --compress=2 \
        --no-header-files \
        --no-man-pages \
        --strip-debug \
        --output /opt/jdk \
        --add-modules \
java.net.http,\
jdk.compiler,\
jdk.sctp,\
java.scripting,\
jdk.unsupported,\
java.base,\
java.compiler,\
java.desktop,\
java.instrument,\
java.management.rmi,\
java.prefs,\
java.security.sasl,\
java.sql,\
jdk.security.auth,\
java.datatransfer,\
java.logging,\
java.naming,\
java.rmi,\
java.sql.rowset,\
java.transaction.xa,\
java.xml,\
java.xml.crypto,\
jdk.xml.dom,\
jdk.crypto.ec,\
jdk.crypto.cryptoki,\
jdk.jfr,\
jdk.naming.dns,\
jdk.zipfs,\
jdk.management.agent \
    && strip --strip-unneeded /opt/jdk/lib/server/libjvm.so

FROM debian:stable-slim AS maven

ARG MAVEN_MAJOR_VERSION=3
ARG MAVEN_VERSION=3.8.1
# Only add the stubbed endpoints if this is a test deployment
ARG DEPLOYMENT_TYPE

# Configure JDK
COPY --from=jdk /opt /opt
ENV JAVA_HOME /opt/jdk
ENV PATH ${PATH}:/opt/jdk/bin
ENV LANG C.UTF-8

# Configure maven
RUN apt-get update && \
    apt-get install --yes curl && \
    apt-get clean all && \
    mkdir --parents /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz https://downloads.apache.org/maven/maven-${MAVEN_MAJOR_VERSION}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn && \
    apt-get remove --yes curl && \
    apt-get autoremove --yes && \
    apt-get clean all

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
    rm -rf /folding-stats/ "${MAVEN_CONFIG}" /usr/share/maven

RUN if [ "${DEPLOYMENT_TYPE}" != "test" ]; then \
       rm -rf /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war; \
    fi

# Override default command (which tries to execute a maven command)
ENTRYPOINT ["bash"]