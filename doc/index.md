# Introduction

**Galeb Manager** is a **Galeb Router** manager built on *Spring Boot*.<br/>
**Galeb Router** is a massively parallel routing system running a shared-nothing architecture.

Its main features are:

- Open Source
- API REST (management)
- Simultaneously managing multiple and different farm types (Galeb Router Farm only, for now)
- Allows dynamically change routes and configuration without having to restart or reload
- Highly scalable

# Important shell variables

- AUTH_METHOD: Authentication method (DEFAULT, LDAP or LDAP_TEST)
- GALEB_LDAP_DN: LDAP User DN pattern (ex.: cn={0},ou=Users,dc=localhost)
- GALEB_LDAP_GROUP_SEARCH: LDAP Group search (ex. ou=Groups,dc=localhost)
- GALEB_LDAP_URL: LDAP server url (ex.: ldap://localhost)
- GALEB_LDAP_USER: LDAP server user (ex.: cn=admin,ou=Users,dc=localhost)
- GALEB_LDAP_PASS: LDAP server password (ex.: password)
---
- DATASOURCE_DIALECT: Hibernate Dialect (ex.: org.hibernate.dialect.MySQL5Dialect)
- DATASOURCE_DDL_AUTO: Hibernate policy DDL (default: validate)
- GALEB_DB_USER: Database user (ex.: root)
- GALEB_DB_PASS: Database password (default: null)
- GALEB_DB_URL: JDBC URL (ex.: jdbc:mysql://localhost/galeb)
- GALEB_DB_SHOWSQL: Hibernate force show SQL (default: false)
- GALEB_DB_DRIVER: JDBC Driver (ex.: com.mysql.jdbc.Driver)
---
- REDIS_HOSTNAME: Redis server hostname (default: localhost)
- REDIS_PORT: Redis server port (default: 6379)
- REDIS_USE_SENTINEL: Enable Sentinel support (default: false)
- REDIS_SENTINEL_MASTER_NAME: Redis Sentinel master name (default: mymaster)
- REDIS_SENTINEL_NODES= Redis Sentinel Nodes (ex.: node1:port,node2:port,etc)

# License

```copyright
Copyright (c) 2014-2015 Globo.com - All rights reserved.

 This source is subject to the Apache License, Version 2.0.
 Please see the LICENSE file for more information.

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
