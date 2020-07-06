#!/usr/bin/env bash
# fail script immediately on any errors in external commands and print the lines
set -ev

export PROFILE=$1

cd rm-automation
ls
mvn install -Pinstall-alfresco,${PROFILE} -q
