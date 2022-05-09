#!/usr/bin/env bash
nexusUrl="${1}"

# shellcheck disable=SC2236
if [[ ! -z "${nexusUrl}" ]]; then
  mkdir --parents /root/.m2

  cat << EOF > /root/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
  <settings xmlns="http://maven.apache.org/SETTINGS/1.1.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd">
    <mirrors>
      <mirror>
        <id>central</id>
        <name>central</name>
        <url>${nexusUrl}/repository/maven-public/</url>
        <mirrorOf>*</mirrorOf>
      </mirror>
    </mirrors>
  </settings>
EOF
else
  echo "Using default settings.xml"
fi
