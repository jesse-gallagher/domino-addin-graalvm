## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.2.0-java11 AS build
COPY --from=domino-docker:V1101_03212020prod /opt/hcl/domino /opt/hcl/domino
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
COPY Notes.jar /usr/src/app/
RUN mvn install:install-file -Dfile=/usr/src/app/Notes.jar -DgroupId=com.hcl.notes.graalvm -DartifactId=Notes -Dversion=11.0.1 -Dpackaging=jar
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
RUN mvn -f /usr/src/app/pom.xml -Pnative clean package

# Run the rest in a Java-friendly world
FROM maven:3.6.3-adoptopenjdk-8
RUN useradd -ms /bin/bash notes

USER root
RUN apt update && apt install -y vim

# Bring in the Domino runtime
COPY --from=domino-docker:V1101_03212020prod /opt/hcl/domino /opt/hcl/domino
RUN mkdir -p /local/notesdata
# TODO check if there's a way to do this in a single ADD
COPY --from=domino-docker:V1101_03212020prod /tmp/notesdata.tbz2 /local/notesdata/
RUN cd /local/notesdata && \
    tar xjf notesdata.tbz2 && \
    rm notesdata.tbz2

# Copy in the built application
COPY --from=build /usr/src/app/target/graalvm-test /opt/hcl/domino/notes/11000100/linux/graalvm-test
RUN chmod +x /opt/hcl/domino/notes/11000100/linux/graalvm-test

# Copy in our Domino server config
COPY --chown=notes:notes docker/notesdata/* /local/notesdata/

WORKDIR /local/notesdata
USER notes
CMD ["/opt/hcl/domino/bin/server"]