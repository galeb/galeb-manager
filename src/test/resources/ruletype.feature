@ruletype
Feature: RuleType Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | rTypeOne |
        And send POST /ruletype

    Scenario: Create RuleType
        Then the response status is 201
        And property name contains rTypeOne

    Scenario: Create duplicated RuleType
        Given a REST client
        When request json body has:
            | name | rTypeOne |
        And send POST /ruletype
        Then the response status is 409

    Scenario: Get RuleType
        Given a REST client
        When send GET /ruletype/1
        Then the response status is 200
        And property name contains rTypeOne

    Scenario: Get null RuleType
        Given a REST client
        When send GET /ruletype/2
        Then the response status is 404

    Scenario: Update RuleType
        Given a REST client
        When request json body has:
            | name | rTypeTwo |
        And send PUT /ruletype/1
        Then the response status is 200
        And property name contains rTypeTwo

    Scenario: Update one field of RuleType
        Given a REST client
        When request json body has:
            | name | rTypeTree |
        And send PATCH /ruletype/1
        Then the response status is 200
        And property name contains rTypeTree

    Scenario: Delete RuleType
        Given a REST client
        When send DELETE /ruletype/1
        Then the response status is 204
