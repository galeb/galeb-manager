Feature: Environment Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path           | body               | status | property | value |
    | POST   | /environment   | { "name": "one" }  | 201    | name     | one   |
    | POST   | /environment   | { "name": "one" }  | 409    |          |       |
    | GET    | /environment/1 |                    | 200    | name     | one   |
    | GET    | /environment/2 |                    | 404    |          |       |
    | PUT    | /environment/1 | { "name": "two" }  | 200    | name     | two   |
    | PATCH  | /environment/1 | { "name": "tree" } | 200    | name     | tree  |
    | DELETE | /environment/1 |                    | 204    |          |       |
