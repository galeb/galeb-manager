Feature: RuleType Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path        | body              | status | property | value |
    | POST   | /ruletype   | { "name": "one" } | 201    | name     | one   |
    | POST   | /ruletype   | { "name": "one" } | 409    |          |       |
    | GET    | /ruletype/1 |                   | 200    | name     | one   |
    | GET    | /ruletype/2 |                   | 404    |          |       |
    | PUT    | /ruletype/1 | { "name": "two" } | 200    | name     | two   |
    | DELETE | /ruletype/1 |                   | 204    |          |       |

