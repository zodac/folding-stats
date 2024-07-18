#!/bin/bash
####################################
# Utility script used to rename all
# files within a directory with a
# hash.
####################################

START_DIRECTORY="/usr/local/apache2/htdocs/res/"
END_DIRECTORY="/usr/local/apache2/htdocs/"

function main() {
  file_extension="${1}"
  directory="${START_DIRECTORY}/${file_extension}/"

  cd "${directory}" || exit 1
  echo "Looking for ${file_extension} files in ${directory}"

  for file_name in *."${file_extension}"
  do
    base=$(basename "${file_name}")
    name_without_extension="${base%%.*}"
    hash=$(md5sum "${file_name}" | tr -s ' ' | cut -d ' ' -f1)
    new_file_name="${name_without_extension}-${hash}.min.${file_extension}"
    mv "${file_name}" "${new_file_name}"
    echo "'${file_name}'"
    grep -rl "${file_name}" "${END_DIRECTORY}" | xargs sed -i "s|${file_name}|${new_file_name}|g"
  done

  cd "${END_DIRECTORY}" || exit 1
}

# Start script execution
set -euo pipefail
main "$@"
