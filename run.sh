#!/usr/bin/env bash

set -e;

docker build -t graalvm-test .

docker run \
	-it \
	--rm \
	graalvm-test