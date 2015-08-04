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
    | method | path         | body                                                                                                                    | status | property | value |
    | POST   | /environment | { "name": "one" }                                                                                                       | 201    | name     | one   |
    | POST   | /farm        | { "name": "one", "available": true, "domain": "domain", "api": "api", "environment": "http://localhost/environment/3" } | 201    | name     | one   |
    | POST   | /farm        | { "name": "one", "available": true, "domain": "domain", "api": "api" }                                                  | 409    |          |       |
    | GET    | /farm/1      |                                                                                                                         | 200    | name     | one   |
    | GET    | /farm/2      |                                                                                                                         | 404    |          |       |
    | PUT    | /farm/1      | { "name": "two", "available":  true, "domain": "domain", "api": "api" }                                                 | 200    | name     | two   |
    | PATCH  | /farm/1      | { "name": "tree" }                                                                                                      | 200    | name     | tree  |
    | DELETE | /farm/1      |                                                                                                                         | 204    |          |       |
