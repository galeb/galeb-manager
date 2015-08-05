Feature: Environment Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /environment

    Scenario: Create Environment
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Environment
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /environment
        Then the response status is 409

    Scenario: Get Environment
        Given a REST client
        When send GET /environment/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Environment
        Given a REST client
        When send GET /environment/2
        Then the response status is 404

    Scenario: Update Environment
        Given a REST client
        When request json body has:
            | name | two |
        And send PUT /environment/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Environment
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /environment/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Environment
        Given a REST client
        When send DELETE /environment/1
        Then the response status is 204
