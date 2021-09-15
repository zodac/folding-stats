FROM jboss/wildfly:24.0.0.Final

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD

RUN /opt/jboss/wildfly/bin/add-user.sh "${ADMIN_USERNAME}" "${ADMIN_PASSWORD}" --silent;

# Configuration files based off Wildfly 24.0.0.Final, may require updates as Wildfly version changes
COPY ./docker/wildfly/config/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml
COPY ./docker/wildfly/config/standalone.conf /opt/jboss/wildfly/bin/standalone.conf

# Set default directory to the log directory
WORKDIR /opt/jboss/wildfly/standalone/log

USER "${ADMIN_USERNAME}"
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]