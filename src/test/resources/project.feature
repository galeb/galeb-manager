Feature: Project Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path        | body              | status | property | value     |
    | POST   | /projects   | { "name": "one" } | 201    | name     | one       |
    | POST   | /projects   | { "name": "one" } | 409    |          |           |
    | GET    | /projects/1 |                   | 200    | name     | one       |
    | GET    | /projects/2 |                   | 404    |          |           |
    | PUT    | /projects/1 | { "name": "two" } | 200    | name     | two       |
    | DELETE | /projects/1 |                   | 204    |          |           |
