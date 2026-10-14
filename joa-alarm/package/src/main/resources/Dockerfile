# docker build -t joa-alarm:24.3.0-REL_1 . [--build-arg baseImage=*]
ARG artifactory_host=${registry.host}
FROM ${artifactory_host}:9000/${hhqs.base.java17.image}:${hhqs.base.java17.image.version}

ENV ENV=container
ENV LANG C.UTF-8

WORKDIR /joa

COPY target/joa-alarm-app-*.jar joa-alarm.jar

EXPOSE 28180

CMD ["java", "-jar", "joa-alarm.jar"]