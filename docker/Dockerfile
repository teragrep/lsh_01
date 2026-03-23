FROM rockylinux/rockylinux:9
RUN --mount=type=bind,source=./rpm/,target=/rpm/ dnf install -y /rpm/*.rpm && dnf clean all
USER srv-lsh_01
COPY docker/docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
