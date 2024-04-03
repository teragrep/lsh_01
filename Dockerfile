FROM rockylinux:8
COPY rpm/target/rpm/com.teragrep-lsh_01/RPMS/noarch/com.teragrep-lsh_01-*.rpm /lsh_01.rpm
RUN dnf install -y /lsh_01.rpm && rm -f /lsh_01.rpm && dnf clean all
COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/opt/teragrep/lsh_01/lib/lsh_01.jar"]
