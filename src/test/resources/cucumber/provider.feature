@provider
Feature: Provider Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | provOne |
        And send POST /provider

    Scenario: Create Provider
        Then the response status is 201

    Scenario: Create duplicated Provider
        Given a REST client
        When request json body has:
            | name | provOne |
        And send POST /provider
        Then the response status is 409

    Scenario: Get Provider
        Given a REST client
        When send GET /provider/1
        Then the response status is 200
        And property name contains provOne

    Scenario: Get null Provider
        Given a REST client
        When send GET /provider/2
        Then the response status is 404

    Scenario: Update Provider
        Given a REST client
        When request json body has:
            | name | provTwo |
        And send PUT /provider/1
        Then the response status is 204
        And a REST client
        When send GET /provider/1
        Then the response status is 200
        And property name contains provTwo

    Scenario: Update one field of Provider
        Given a REST client
        When request json body has:
            | name | provThree |
        And send PATCH /provider/1
        Then the response status is 204
        And a REST client
        When send GET /provider/1
        Then the response status is 200
        And property name contains provThree

    Scenario: Delete Provider
        Given a REST client
        When send DELETE /provider/1
        Then the response status is 204
        And a REST client
        When send GET /provider/1
        Then the response status is 404