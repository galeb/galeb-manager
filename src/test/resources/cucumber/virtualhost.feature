@virtualhost
Feature: VirtualHost Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /environment
        And a REST client
        When request json body has:
            | name | one |
        And send POST /project
        And a REST client
        When request json body has:
            | name        | one                            |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost

    Scenario: Create VirtualHost
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Environment
        Given a REST client
        When request json body has:
            | name        | one                            |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost
        Then the response status is 409

    Scenario: Get VirtualHost
        Given a REST client
        When send GET /virtualhost/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null VirtualHost
        Given a REST client
        When send GET /virtualhost/2
        Then the response status is 404

    Scenario: Update VirtualHost
        Given a REST client
        When request json body has:
            | name        | two                            |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send PUT /virtualhost/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Environment
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /virtualhost/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Environment
        Given a REST client
        When send DELETE /virtualhost/1
        Then the response status is 204
