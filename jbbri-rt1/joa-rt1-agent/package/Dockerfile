# docker build -t joa-rt1agent:24.3.0-REL_1 . [--build-arg baseImage=*]
ARG baseImage=artifactory-blr-hhqs.int.net.x.com:9000/hhqs-base-java17:x-1.1.9
FROM ${baseImage}
ENV ENV=container
ENV LANG C.UTF-8

WORKDIR /joa

COPY target/joa-rt1-agent-app-*.jar joa-rt1-agent.jar

EXPOSE 28080 12015

CMD ["java", "-jar", "joa-rt1-agent.jar"]
