Feature: TargetType Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path          | body              | status | property | value |
    | POST   | /targettype   | { "name": "one" } | 201    | name     | one   |
    | POST   | /targettype   | { "name": "one" } | 409    |          |       |
    | GET    | /targettype/1 |                   | 200    | name     | one   |
    | GET    | /targettype/2 |                   | 404    |          |       |
    | PUT    | /targettype/1 | { "name": "two" } | 200    | name     | two   |
    | DELETE | /targettype/1 |                   | 204    |          |       |

