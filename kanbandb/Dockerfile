FROM mysql/mysql-server

ENV MYSQL_RANDOM_ROOT_PASSWORD=true
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=mysql
ENV MYSQL_DATABASE=kanbandb
ENV TZ=UTC

COPY scripts/ /docker-entrypoint-initdb.d/