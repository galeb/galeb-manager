# Required

- Java 8
- Maven 3.3
- Redis 2.8
- MySQL 5.6

# Preparing test database

```
mysql -u root -e 'create database galeb'
```

# Testing using MySQL (optional)

```
export GALEB_DB_USER=root
export GALEB_DB_URL=jdbc:mysql://localhost/galeb
export GALEB_DB_SHOWSQL=false
export GALEB_DB_DRIVER=com.mysql.jdbc.Driver
export GALEB_DB_DIALECT=org.hibernate.dialect.MySQL5Dialect
export GALEB_DB_DDL_AUTO=validate

mvn clean test
```

# Building

```
mvn clean package -DskipTests
```
