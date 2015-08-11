@team
Feature: Team Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /team

    Scenario: Create Team
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Team
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /team
        Then the response status is 409

    Scenario: Get Team
        Given a REST client
        When send GET /team/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Team
        Given a REST client
        When send GET /team/2
        Then the response status is 404

    Scenario: Update Team
        Given a REST client
        When request json body has:
            | name | two |
        And send PUT /team/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Team
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /team/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Team
        Given a REST client
        When send DELETE /team/1
        Then the response status is 204
