FROM maven:3.8.1-openjdk-11

RUN mkdir --parents /folding-stats/
WORKDIR /folding-stats/

# Copy source code
COPY ./folding-stats-api ./folding-stats-api
COPY ./folding-stats-client-library-java ./folding-stats-client-library-java
COPY ./folding-stats-jar ./folding-stats-jar
COPY ./folding-stats-war ./folding-stats-war
# Test logic must be added, or else the 'mvn install' will fail
COPY ./testsuite ./testsuite
COPY ./stubbed-folding-endpoints-war ./stubbed-folding-endpoints-war

# Build artifacts
RUN mvn clean install

# Output
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/
RUN mv ./folding-stats-war/target/folding-stats-war*.war /opt/jboss/wildfly/standalone/deployments/folding-stats-war.war

# Override default command (which tries to execute a maven command)
ENTRYPOINT ["bash"]