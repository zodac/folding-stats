#!/usr/bin/env bash

instance_type="test"
if [[ "${DEPLOYMENT_TYPE}" == "production" ]]; then
  instance_type="production"
else
  instance_type="test"
fi

echo "Starting ${instance_type} instance"

# 'add-opens' is needed to allow Gson to serialise 'Collections.emptyList()'
java \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -jar \
  -Dspring.profiles.active="${instance_type}" \
  /folding-stats.jar
