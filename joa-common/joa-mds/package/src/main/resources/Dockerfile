ARG artifactory_host=${registry.host}
FROM ${artifactory_host}:9000/${hhqs.base.java17.image}:${hhqs.base.java17.image.version}

ENV LANG C.UTF-8
ENV ENV=container

RUN mkdir -p /joa/mds
WORKDIR /joa/mds
COPY target/joa-mds-${revision}.jar /joa/mds/joa-mds.jar

CMD ["java", "-jar", "/joa/mds/joa-mds.jar"]
