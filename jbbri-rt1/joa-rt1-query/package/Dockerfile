# docker build -t joa-rt1query:24.3.0-REL_1 . [--build-arg baseImage=*]
ARG baseImage=artifactory-blr-hhqs.int.net.x.com:9000/hhqs-base-java17:x-1.1.9
FROM ${baseImage}
ENV ENV=container
ENV LANG C.UTF-8

WORKDIR /joa

COPY target/joa-rt1-query-app-*.jar joa-rt1-query.jar
COPY target/unpacked-jar/BOOT-INF/classes/customField.xml default/customField.xml
COPY target/unpacked-jar/BOOT-INF/classes/templates default/templates
COPY target/unpacked-jar/BOOT-INF/classes/commands default/commands
COPY target/unpacked-jar/BOOT-INF/classes/passthrough default/passthrough

VOLUME /joa/config

EXPOSE 28082

CMD ["java", "-jar", "joa-rt1-query.jar"]