# Special JVM parameters

- -DINTERNAL_PASSWORD: Force admin buildin password
- -Dauth_method: Override AUTH_METHOD shell variable
- -Dlog4j.configurationFile: Log4j config file path
- -Dserver.port: Galeb Manager server port

Galeb Manager is a Spring Boot Powered application. All Spring JVM properties are supported.  
See more: [Spring Common Application Properties](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)

# Using spring-boot plugin

```
export GALEB_DB_USER=root
export GALEB_DB_URL=jdbc:mysql://localhost/galeb
export GALEB_DB_SHOWSQL=false
export GALEB_DB_DRIVER=com.mysql.jdbc.Driver
export GALEB_DB_DIALECT=org.hibernate.dialect.MySQL5Dialect
export GALEB_DB_DDL_AUTO=validate

mvn spring-boot:run
```

# Running directly with java -jar

```
export GALEB_DB_USER=root
export GALEB_DB_URL=jdbc:mysql://localhost/galeb
export GALEB_DB_SHOWSQL=false
export GALEB_DB_DRIVER=com.mysql.jdbc.Driver
export GALEB_DB_DIALECT=org.hibernate.dialect.MySQL5Dialect
export GALEB_DB_DDL_AUTO=validate

java -jar target/galeb-manager-<version>.jar -server \
          -Xmx2048m -Xms2048m \
          -Dlog4j.configurationFile=log4j.xml \
          -Dauth_method=DEFAULT \
          -Dserver.port=8000 \
          -DINTERNAL_PASSWORD=password
```
