#!/bin/sh

rm -rf report cop5 COUNTER_SUSHI_API.json
git clone -b 5.1 https://github.com/Project-Counter/cop5.git

mkdir report
cp cop5/source/_static/report/*.json report
cp cop5/sushi-api/COUNTER_SUSHI_API.json ./

rm -rf cop5
