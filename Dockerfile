FROM eclipse-temurin:11-alpine
WORKDIR /usr/src/app

ARG USERNAME
ARG UID
ARG GID
ARG GROUPNAME

ENV LANG=en_US.UTF-8 \
    MIN_JAVA_HEAP_MEM_SIZE=200m \
    MAX_JAVA_HEAP_MEM_SIZE=4g \
    MAX_JAVA_THREAD_STACK_MEM_SIZE=64m \
    JAVA_HEAP_DUMP_OPTIONS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/hdump.hprof"

# https://bugzilla.redhat.com/show_bug.cgi?id=1940902 / https://issues.redhat.com/browse/OPENJDK-335
ENV NSS_WRAPPER_PASSWD=
ENV NSS_WRAPPER_GROUP=

RUN groupadd --system --gid ${GID} ${GROUPNAME} && useradd --system --uid ${UID} --gid ${GID} ${USERNAME}

RUN mkdir /files/
RUN chown ${USERNAME}:${GROUPNAME} /files/
RUN chmod -R ug+rwx /files/

USER ${USERNAME}

COPY ./target/file-extraction-processor-1.0-jar-with-dependencies.jar file-extraction-processor.jar

ENTRYPOINT java -javaagent:/var/tmp/elastic-apm-agent-1.28.1.jar \
            -Delastic.apm.enable_log_correlation=true \
            -Delastic.apm.service_name=file-extractor-service \
            -Delastic.apm.application_packages=com.dsv \
            -Delastic.apm.server_url=${ELASTIC_APM_SERVER_URL}\
            -jar file-extraction-processor.jar
