Feature: VirtualHost Support
    The manager have than
    to support REST standard

    Scenario Outline: API action
        Given a REST client
        When request body is <body>
        And send <method> <path>
        Then the response status is <status>
        And property <property> contains <value>

    Examples:
    | method | path           | body                                                                       | status | property | value           |
    | POST   | /environment   | { "name": "env_virtualhost" }                                              | 201    | name     | env_virtualhost |
    | POST   | /virtualhost   | { "name": "localdomain", "environment": "http://localhost/environment/4" } | 201    | name     | localdomain     |
    | POST   | /virtualhost   | { "name": "localdomain" }                                                  | 409    |          |                 |
    | GET    | /virtualhost/1 |                                                                            | 200    | name     | localdomain     |
    | GET    | /virtualhost/2 |                                                                            | 404    |          |                 |
    | PUT    | /virtualhost/1 | { "name": "localdomain_new" }                                              | 200    | name     | localdomain_new |
    | PATCH  | /virtualhost/1 | { "name": "localdomain_old" }                                              | 200    | name     | localdomain_old |
    | DELETE | /virtualhost/1 |                                                                            | 204    |          |                 |
