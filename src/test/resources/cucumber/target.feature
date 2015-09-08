@target
Feature: Target Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | tTypeTwo |
        And send POST /targettype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | envOne |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | teamOne |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
            | email    | test@fake.com               |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | targetOne                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /target

    Scenario: Create Target
        Then the response status is 201

    Scenario: Create Target with Parent
        Given a REST client authenticated as accountOne
        And request json body has:
            | name       | newTargetTwo                  |
            | targetType | http://localhost/targettype/1 |
            | parent     | http://localhost/target/1     |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When send GET /target/2/environment/1
        Then the response status is 200
        And property name contains envOne
        And a REST client authenticated as accountOne
        When send GET /target/2/project/1
        Then the response status is 200
        And property name contains projOne

    Scenario: Create Target with Parent and Project inconsistent
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projTwo                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name       | newTargetTwo                  |
            | targetType | http://localhost/targettype/1 |
            | parent     | http://localhost/target/1     |
            | project    | http://localhost/project/2    |
        And send POST /target
        Then the response status is 400

    Scenario: Create Target with Parent and Environment inconsistent
        Given a REST client authenticated as admin
        When request json body has:
            | name  | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | newTargetTwo                   |
            | targetType  | http://localhost/targettype/1  |
            | parent      | http://localhost/target/1      |
            | environment | http://localhost/environment/2 |
        And send POST /target
        Then the response status is 400

    Scenario: Create duplicated Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetOne                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /target
        Then the response status is 409

    Scenario: Get Target
        Given a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Get null Target
        Given a REST client authenticated as accountOne
        When send GET /target/3
        Then the response status is 404

    Scenario: Update Target (name update is ignored)
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo                      |
            | targetType  | http://localhost/targettype/1  |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send PUT /target/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Update name field of Target (name update is ignored)
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | targetThree |
        And send PATCH /target/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Update targetType field of Target (name update is ignored)
        Given a REST client authenticated as accountOne
        When request json body has:
            | targetType  | http://localhost/targettype/2 |
        And send PATCH /target/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Delete Target
        Given a REST client authenticated as accountOne
        When send DELETE /target/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET /targettype/1
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET /environment/1
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 200
