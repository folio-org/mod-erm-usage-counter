#!/bin/sh

# Constants
REPO_URL="https://github.com/Project-Counter/cop5.git"
REPO_BRANCH="planned"
TEMP_DIR="/tmp/cop5"
REPORTS_SOURCE_DIR="${TEMP_DIR}/source/_static/report"
API_SOURCE_PATH="${TEMP_DIR}/api-specification/COUNTER_API.json"
REPORTS_TARGET_DIR="src/test/resources/sample-reports"
API_TARGET_DIR="src/main/resources"

echo "-- Cloning cop5 repository to ${TEMP_DIR}."
git clone -b ${REPO_BRANCH} ${REPO_URL} ${TEMP_DIR}

echo "-- Copying files."
cp ${REPORTS_SOURCE_DIR}/*.json ${REPORTS_TARGET_DIR}
cp ${REPORTS_SOURCE_DIR}/*.tsv ${REPORTS_TARGET_DIR}
find ${REPORTS_SOURCE_DIR} \
  -regextype posix-extended \
  -regex ".*/(TR|DR|IR|PR)_sample.*\.xlsx" \
  -exec cp {} ${REPORTS_TARGET_DIR} \;
cp ${API_SOURCE_PATH} ${API_TARGET_DIR}

echo "-- Done."