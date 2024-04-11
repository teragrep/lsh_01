FROM rockylinux:8
COPY rpm/target/rpm/com.teragrep-lsh_01/RPMS/noarch/com.teragrep-lsh_01-*.rpm /lsh_01.rpm
RUN dnf install -y /lsh_01.rpm && rm -f /lsh_01.rpm && dnf clean all
USER srv-lsh_01
COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
