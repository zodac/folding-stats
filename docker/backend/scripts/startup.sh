#!/busybox sh

instance_type="test"
if [ "${DEPLOYMENT_TYPE}" = "production" ]; then
  instance_type="production"
fi

echo "Starting ${instance_type} instance"

exec /opt/jdk/bin/java \
  -Xms"${JVM_MIN}" -Xmx"${JVM_MAX}" \
  -Dspring.profiles.active="${instance_type}" \
  -Dlogging.config=/var/backend/log4j2.xml \
  -jar /folding-stats.jar
