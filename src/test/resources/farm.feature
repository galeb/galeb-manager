Feature: Farm Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path         | body                                                                                                                      | status | property | value    |
    | POST   | /environment | { "name": "env_farm" }                                                                                                    | 201    | name     | env_farm |
    | POST   | /farm        | { "name": "farm1", "available": true, "domain": "domain", "api": "api", "environment": "http://localhost/environment/3" } | 201    | name     | farm1    |
    | POST   | /farm        | { "name": "farm1", "available": true, "domain": "domain", "api": "api" }                                                  | 409    |          |          |
    | GET    | /farm/1      |                                                                                                                           | 200    | name     | farm1    |
    | GET    | /farm/2      |                                                                                                                           | 404    |          |          |
    | PUT    | /farm/1      | { "name": "farm2", "available":  true, "domain": "domain", "api": "api" }                                                 | 200    | name     | farm2    |
    | PATCH  | /farm/1      | { "name": "farm3" }                                                                                                       | 200    | name     | farm3    |
    | DELETE | /farm/1      |                                                                                                                           | 204    |          |          |
