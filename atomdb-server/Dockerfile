FROM docker.phonepe.com:5000/pp-focal-openjdk-17:1.0.0

EXPOSE 8080
EXPOSE 8081

# ratis port
EXPOSE 9090

VOLUME /var/log/atomdb-server

ADD target/atomdb-server.jar atomdb-server.jar

ENTRYPOINT exec java -jar -XX:+${GC_ALGO-UseG1GC} -Xms${JAVA_PROCESS_MIN_HEAP-1g} -Xmx${JAVA_PROCESS_MAX_HEAP-1g} ${JAVA_OPTS} atomdb-server.jar server /rosey/config.yml