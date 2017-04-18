FROM maven:3-jdk-8

ENV GALEB_DB_URL jdbc:mysql://mysql.local:3306/galeb_test
ENV GALEB_DB_DRIVER com.mysql.jdbc.Driver
ENV GALEB_DB_USER root
ENV GALEB_DB_PASS password
ENV REDIS_HOSTNAME redis.local

EXPOSE 8000

RUN git clone https://github.com/galeb/galeb-manager.git && \
    mvn clean package -DskipTests -f galeb-manager/pom.xml

WORKDIR galeb-manager

CMD java -jar target/galeb-manager-*-SNAPSHOT.jar
