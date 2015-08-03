Feature: Type Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path    | body              | status | property | value |
    | POST   | /type   | { "name": "one" } | 201    | name     | one   |
    | POST   | /type   | { "name": "one" } | 409    |          |       |
    | GET    | /type/1 |                   | 200    | name     | one   |
    | GET    | /type/2 |                   | 404    |          |       |
    | PUT    | /type/1 | { "name": "two" } | 200    | name     | two   |
    | DELETE | /type/1 |                   | 204    |          |       |
