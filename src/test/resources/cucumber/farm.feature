@farm
Feature: Farm Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | envOne |
        And send POST /environment
        And a REST client
        When request json body has:
            | name | providerOne |
        And send POST /provider
        And a REST client
        When request json body has:
            | name        | farmOne                        |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm

    Scenario: Create Farm
        Then the response status is 201

    Scenario: Create duplicated Farm
        Given a REST client
        When request json body has:
            | name        | farmOne                        |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm
        Then the response status is 409

    Scenario: Get Farm
        Given a REST client
        When send GET /farm/1
        Then the response status is 200
        And property name contains farmOne

    Scenario: Get null Farm
        Given a REST client
        When send GET /farm/2
        Then the response status is 404

    Scenario: Update Farm
        Given a REST client
        When request json body has:
            | name        | farmTwo                        |
            | domain      | domainTwo                      |
            | api         | apiTwo                         |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send PUT /farm/1
        Then the response status is 204
        And a REST client
        When send GET /farm/1
        Then the response status is 200
        And property name contains farmTwo
        And property domain contains domainTwo
        And property api contains apiTwo

    Scenario: Update one field of Farm
        Given a REST client
        When request json body has:
            | name | farmThree |
        And send PATCH /farm/1
        Then the response status is 204
        And a REST client
        When send GET /farm/1
        Then the response status is 200
        And property name contains farmThree
        And property domain contains domain

    Scenario: Delete Farm
        Given a REST client
        When send DELETE /farm/1
        Then the response status is 204
        And a REST client
        When send GET /farm/1
        Then the response status is 404