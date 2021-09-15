FROM jboss/wildfly:24.0.0.Final

ARG ADMIN_USERNAME
ARG ADMIN_PASSWORD

RUN /opt/jboss/wildfly/bin/add-user.sh "${ADMIN_USERNAME}" "${ADMIN_PASSWORD}" --silent;

# Configuration file is based off Wildfly 24.0.0.Final, may require updates as Wildfly version changes
COPY ./docker/wildfly/config/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

# Set default directory to the log directory
WORKDIR /opt/jboss/wildfly/standalone/log

USER "${ADMIN_USERNAME}"
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]