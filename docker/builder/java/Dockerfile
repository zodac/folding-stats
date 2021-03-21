FROM maven:3.6.3-openjdk-11

RUN mkdir --parents /timezone-terraform/
WORKDIR /timezone-terraform/

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
COPY ./timezone-terraform-api/pom.xml ./timezone-terraform-api/pom.xml
COPY ./timezone-terraform-ear/pom.xml ./timezone-terraform-ear/pom.xml
COPY ./timezone-terraform-jar/pom.xml ./timezone-terraform-jar/pom.xml
COPY ./timezone-terraform-war/pom.xml ./timezone-terraform-war/pom.xml
COPY ./testsuite/pom.xml ./testsuite/pom.xml

RUN mvn verify clean --fail-never -DskipTests


# Copy source code
COPY ./timezone-terraform-api ./timezone-terraform-api
COPY ./timezone-terraform-ear ./timezone-terraform-ear
COPY ./timezone-terraform-jar ./timezone-terraform-jar
COPY ./timezone-terraform-war ./timezone-terraform-war
# Not used to build the output, but listed in the root pom.xml, so needs to be added for mvn to pass
COPY ./testsuite ./testsuite

# Build artifacts
RUN mvn clean install

# Output
RUN mkdir --parents /opt/jboss/wildfly/standalone/deployments/
RUN mv ./timezone-terraform-ear/target/*.ear /opt/jboss/wildfly/standalone/deployments/timezone-terraform-ear.ear

# Override default command (which tries to execute a maven command)
ENTRYPOINT ["bash"]