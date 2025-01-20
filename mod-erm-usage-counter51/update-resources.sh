#!/bin/sh

echo "-- Cloning cop5 repository to /tmp/cop5."
git clone -b planned https://github.com/Project-Counter/cop5.git /tmp/cop5

echo "-- Copying files."
cp /tmp/cop5/source/_static/report/*.json src/test/resources/sample-reports
cp /tmp/cop5/source/_static/report/*.tsv src/test/resources/sample-reports
cp /tmp/cop5/api-specification/COUNTER_API.json src/main/resources

echo "-- Done."
