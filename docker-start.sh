#!/bin/bash

mvn compile flyway:clean -Dflyway.user=root -Dflyway.password=password -Dflyway.url=$GALEB_DB_URL
mvn compile flyway:migrate -Dflyway.user=root -Dflyway.password=password -Dflyway.url=$GALEB_DB_URL
mvn spring-boot:run
