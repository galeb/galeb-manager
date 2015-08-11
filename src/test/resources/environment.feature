@environment
Feature: Environment Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | envOne |
        And send POST /environment

    Scenario: Create Environment
        Then the response status is 201
        And property name contains envOne

    Scenario: Create duplicated Environment
        Given a REST client
        When request json body has:
            | name | envOne |
        And send POST /environment
        Then the response status is 409

    Scenario: Get Environment
        Given a REST client
        When send GET /environment/1
        Then the response status is 200
        And property name contains envOne

    Scenario: Get null Environment
        Given a REST client
        When send GET /environment/2
        Then the response status is 404

    Scenario: Update Environment
        Given a REST client
        When request json body has:
            | name | envTwo |
        And send PUT /environment/1
        Then the response status is 200
        And property name contains envTwo

    Scenario: Update one field of Environment
        Given a REST client
        When request json body has:
            | name | envTree |
        And send PATCH /environment/1
        Then the response status is 200
        And property name contains envTree

    Scenario: Delete Environment
        Given a REST client
        When send DELETE /environment/1
        Then the response status is 204
