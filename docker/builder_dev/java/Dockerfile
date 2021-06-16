FROM maven:3.8.1-openjdk-11

RUN mkdir --parents /folding-stats/
WORKDIR /folding-stats/

# Copy source code
COPY ./pom.xml ./pom.xml
COPY ./folding-stats-api ./folding-stats-api
COPY ./folding-stats-client-library-java ./folding-stats-client-library-java
COPY ./folding-stats-jar ./folding-stats-jar
COPY ./folding-stats-war ./folding-stats-war
COPY ./stubbed-folding-endpoints-war ./stubbed-folding-endpoints-war
# Unused poms must also be added, or else 'mvn install' will not even start
COPY ./testsuite/pom.xml ./testsuite/pom.xml

# Build artifacts
RUN mvn install

# Output
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/
RUN mv ./folding-stats-war/target/folding-stats-war*.war /opt/jboss/wildfly/standalone/deployments/folding-stats-war.war
RUN mv ./stubbed-folding-endpoints-war/target/stubbed-folding-endpoints-war*.war /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war

# Override default command (which tries to execute a maven command)
ENTRYPOINT ["bash"]