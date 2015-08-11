@rule
Feature: Rule Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /ruletype
        And the response status is 201
        And a REST client
        When request json body has:
            | name | envOne |
        And send POST /environment
        And a REST client
        When request json body has:
            | name | projectOne |
        And send POST /project
        And a REST client
        When request json body has:
            | name | providerOne |
        And send POST /provider
        And a REST client
        When request json body has:
            | name        | one                            |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm
        And a REST client
        When request json body has:
            | name        | virtOne                        |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost
        And the response status is 201
        And a REST client
        And request json body has:
            | name     | one                            |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
        And send POST /rule

    Scenario: Create Rule
        Then the response status is 201
        And property name contains one

    Scenario: Create Rule without parent
        Then the response status is 201
        And a REST client
        When request json body has:
            | name        | RulewithOutParent              |
            | ruleType    | http://localhost/ruletype/1    |
            | environment | http://localhost/environment/1 |
        And send POST /rule
        Then the response status is 201

    Scenario: Create duplicated Rule
        Then the response status is 201
        And a REST client
        When request json body has:
            | name     | one                            |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
        And send POST /rule
        Then the response status is 409

    Scenario: Get Rule
        Then the response status is 201
        And a REST client
        When send GET /rule/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Rule
        Given a REST client
        When send GET /rule/2
        Then the response status is 404

    Scenario: Update Rule
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name     | two                            |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
        And send PUT /rule/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Rule
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name | two |
        And send PATCH /rule/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Rule
        Then the response status is 201
        And property name contains one
        And a REST client
        When send DELETE /rule/1
        Then the response status is 204
