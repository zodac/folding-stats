#!/usr/bin/env bash

if [[ "${DEPLOYMENT_TYPE}" == "production" ]]; then
  echo "Starting production instance"
  java -jar -Dspring.profiles.active=production /folding-stats.jar
else
  echo "Starting test instance"
  java -jar -Dspring.profiles.active=test /folding-stats.jar
fi