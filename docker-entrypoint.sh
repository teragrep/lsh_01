#!/bin/bash
if [ ! -f /config/config.properties ]; then
    echo "ERROR: Missing '/config/config.properties', refusing to continue";
    exit 1;
fi;

if [ ! -f /config/credentials.json ]; then
    echo "ERROR: Missing '/config/credentials.json', refusing to continue";
    exit 1;
fi;

if [ -f /config/log4j2.xml ]; then
    LOG4J_FILE="/config/log4j2.xml";
else
    echo "INFO: Missing '/config/log4j2.xml', using default logger config";
    LOG4J_FILE="/opt/teragrep/lsh_01/etc/log4j2.xml";
fi;

exec /usr/bin/java -Dproperties.file=/config/config.properties -Dcredentials.file=/config/credentials.json -Dlog4j2.configurationFile=file:"${LOG4J_FILE}" -jar /opt/teragrep/lsh_01/share/lsh_01.jar
