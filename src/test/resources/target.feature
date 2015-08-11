@target
Feature: Target Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype
        And a REST client
        When request json body has:
            | name | envOne |
        And send POST /environment
        And a REST client
        When request json body has:
            | name | projOne |
        And send POST /project
        And a REST client
        When request json body has:
            | name        | virtOne                        |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost
        And a REST client
        And request json body has:
            | name        | targetOne                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environemtn/1 |
        And send POST /target

    Scenario: Create Target
        Then the response status is 201
        And property name contains targetOne
        
    Scenario: Create Target with Parent
        Given a REST client
        And request json body has:
            | name       | newTargetTwo                  |
            | targetType | http://localhost/targettype/1 |
            | parent     | http://localhost/target/2     |
        And send POST /target
        Then the response status is 201
        And property name contains newTargetTwo

    Scenario: Create duplicated Target
        Given a REST client
        When request json body has:
            | name        | targetOne                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environemtn/1 |
        And send POST /target
        Then the response status is 409

    Scenario: Get Target
        Given a REST client
        When send GET /target/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Get null Target
        Given a REST client
        When send GET /target/3
        Then the response status is 404

    Scenario: Update Target
        Given a REST client
        When request json body has:
            | name        | targetTwo                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environemtn/1 |
        And send PUT /target/1
        Then the response status is 200
        And property name contains targetTwo

    Scenario: Update one field of Target
        Given a REST client
        When request json body has:
            | name | targetTree |
        And send PATCH /target/1
        Then the response status is 200
        And property name contains targetTree

    Scenario: Delete Target
        Given a REST client
        When send DELETE /target/1
        Then the response status is 204
