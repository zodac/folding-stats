FROM maven:3.8.1-openjdk-11

RUN mkdir --parents /folding-stats/
WORKDIR /folding-stats/

# Copying all the pom.xml files so we can run a 'mvn verify clean', which will download all dependencies.
# We do this before copying the source code to cache the docker layer.
# If we change the source code, we won't need to re-download the dependencies.
# However, any changes to the pom.xmls will invalidate the docker layer and require a rebuild.
#
# Failures in the mvn command can be ignored, we are only interested in downloading the dependencies.
#
# In a real scenario, probably would build the artifacts locally and copy into
# some test container in a CICD flow, to leverage the local .m2 on the Jenkins server.
COPY ./pom.xml ./pom.xml
COPY ./folding-stats-api/pom.xml ./folding-stats-api/pom.xml
COPY ./folding-stats-client-library-java/pom.xml ./folding-stats-client-library-java/pom.xml
COPY ./folding-stats-jar/pom.xml ./folding-stats-jar/pom.xml
COPY ./folding-stats-war/pom.xml ./folding-stats-war/pom.xml
COPY ./folding-stats-ear/pom.xml ./folding-stats-ear/pom.xml
COPY ./docker/pom.xml ./docker/pom.xml
COPY ./stubbed-folding-endpoints-war/pom.xml ./stubbed-folding-endpoints-war/pom.xml
COPY ./testsuite/pom.xml ./testsuite/pom.xml

RUN mvn verify clean --fail-never -DskipTests

# Copy source code
COPY ./folding-stats-api ./folding-stats-api
RUN cd ./folding-stats-api && mvn clean install; cd ..

COPY ./folding-stats-client-library-java ./folding-stats-client-library-java
RUN cd ./folding-stats-client-library-java && mvn clean install; cd ..

COPY ./folding-stats-jar ./folding-stats-jar
RUN cd ./folding-stats-jar && mvn clean install; cd ..

COPY ./folding-stats-war ./folding-stats-war
RUN cd ./folding-stats-war && mvn clean install; cd ..

COPY ./folding-stats-ear ./folding-stats-ear
RUN cd ./folding-stats-ear && mvn clean install; cd ..

COPY ./stubbed-folding-endpoints-war ./stubbed-folding-endpoints-war
RUN cd ./stubbed-folding-endpoints-war && mvn clean install; cd ..

COPY ./testsuite ./testsuite
RUN cd ./testsuite && mvn clean install; cd ..

# Build artifacts
RUN mvn clean install

# Output
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/
RUN mv ./folding-stats-ear/target/folding-stats-ear*.ear /opt/jboss/wildfly/standalone/deployments/folding-stats-ear.ear
RUN mv ./stubbed-folding-endpoints-war/target/stubbed-folding-endpoints-war*.war /opt/jboss/wildfly/standalone/deployments/stubbed-folding-endpoints-war.war

# Override default command (which tries to execute a maven command)
ENTRYPOINT ["bash"]