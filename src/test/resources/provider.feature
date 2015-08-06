Feature: Provider Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /provider

    Scenario: Create Provider
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Provider
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /provider
        Then the response status is 409

    Scenario: Get Provider
        Given a REST client
        When send GET /provider/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Provider
        Given a REST client
        When send GET /provider/2
        Then the response status is 404

    Scenario: Update Provider
        Given a REST client
        When request json body has:
            | name | two |
        And send PUT /provider/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Provider
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /provider/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Provider
        Given a REST client
        When send DELETE /provider/1
        Then the response status is 204
