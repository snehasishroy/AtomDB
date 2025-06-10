#!/usr/bin/env bash
export MAVEN_OPTS="-Xms1024m -Xmx6096m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
if [[ "$1" = 'd' ]]
then cd .. && mvn clean install -DskipTests -Plocal -pl !atomdb-server && cd atomdb-server
fi
mvn compile -Plocal exec:java -Dexec.mainClass="com.phonepe.platform.atomdb.server.AtomDbApplication" -Duser.timezone=IST -DlocalConfig=true -Dexec.args="server config/local.yml"
#mvn clean install -DskipTests -Plocal && java -jar -XX:+UseG1GC -Xms1g -Xmx1g -DlocalConfig=true -Ddb.shards=1 target/atomdb-server-1.0-SNAPSHOT.jar server config/local.yml