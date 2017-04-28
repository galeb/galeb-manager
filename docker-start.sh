#!/bin/bash

export GALEB_DB_URL=jdbc:mysql://127.0.0.1:3306/galeb_test
export GALEB_DB_DRIVER=com.mysql.jdbc.Driver
export GALEB_DB_USER=root
export GALEB_DB_PASS=password
export REDIS_HOSTNAME=127.0.0.1

mvn clean package -DskipTests
[ "x$1" == "xreset" ] && mvn compile flyway:clean -Dflyway.user=root -Dflyway.password=password -Dflyway.url=$GALEB_DB_URL
[ "x$1" == "xreset" ] && mvn compile flyway:migrate -Dflyway.user=root -Dflyway.password=password -Dflyway.url=$GALEB_DB_URL
mvn spring-boot:run
