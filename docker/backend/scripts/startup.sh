#!/usr/bin/env bash

instance_type="test"
if [[ "${DEPLOYMENT_TYPE}" == "production" ]]; then
  instance_type="production"
fi

echo "Starting ${instance_type} instance"

java \
  -jar \
  -Xms"${JVM_MIN}" -Xmx"${JVM_MAX}" \
  -Dspring.profiles.active="${instance_type}" \
  /folding-stats.jar
