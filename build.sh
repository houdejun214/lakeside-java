#!/bin/bash

echo "[Pre-Requirement] Makesure install JDK 6.0+ and set the JAVA_HOME."
echo "[Pre-Requirement] Makesure install Maven 3.0.3+ and set the PATH."

action=$1

if [ -z "$action" ]
  then
    echo "No action supplied,should like: build.sh [install|deploy|package]"
fi
        
set MAVEN_OPTS=$MAVEN_OPTS -XX:MaxPermSize=128m

mvn clean $action -Dmaven.test.skip=true

echo "[Step 1] Install core modules to local maven repository."
cd lakeside-core
mvn clean $action -Dmaven.test.skip=true

cd ../lakeside-data
mvn clean $action -Dmaven.test.skip=true

cd ../lakeside-web
mvn clean $action -Dmaven.test.skip=true
