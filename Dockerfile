FROM rockylinux:8
COPY rpm/target/rpm/com.teragrep-lsh_01/RPMS/noarch/com.teragrep-lsh_01-*.rpm /lsh_01.rpm
RUN dnf install -y /lsh_01.rpm && rm -f /lsh_01.rpm && dnf clean all
USER srv-lsh_01
ENTRYPOINT ["/usr/bin/java"]
CMD ["-Dproperties.file=/config/config.properties", "-Dlog4j2.configurationFile=file:/config/log4j2.xml", "-jar", "/opt/teragrep/lsh_01/share/lsh_01.jar"]
