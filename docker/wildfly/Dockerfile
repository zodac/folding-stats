FROM jboss/wildfly:22.0.1.Final
# TODO: [zodac] Pass in username/password from env
RUN /opt/jboss/wildfly/bin/add-user.sh root shroot --silent;

# Configuration file is based off Wildfly 22.0.1.Final, may require updates as Wildfly version changes
COPY ./config/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

USER root
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]