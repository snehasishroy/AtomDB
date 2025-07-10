FROM docker.phonepe.com/ci/ubuntu/jammy/jdk/openjdk/17/maven/3.9.9:latest


EXPOSE 8080
EXPOSE 8081

# ratis port
EXPOSE 9090

VOLUME /var/log/atomdb-server

ADD target/atomdb-server.jar atomdb-server.jar

ENTRYPOINT exec java -jar -XX:+${GC_ALGO-UseG1GC} -Xms${JAVA_PROCESS_MIN_HEAP-1g} -Xmx${JAVA_PROCESS_MAX_HEAP-1g} ${JAVA_OPTS} atomdb-server.jar server /rosey/config.yml