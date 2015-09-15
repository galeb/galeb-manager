@targettype
Feature: TargetType Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
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
        And a REST client authenticated as admin
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype

    Scenario: Create TargetType
        Then the response status is 201

    Scenario: Create duplicated TargetType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype
        Then the response status is 409

    Scenario: Get TargetType
        Given a REST client authenticated as accountOne
        When send GET TargetType=tTypeOne
        Then the response status is 200
        And property name contains tTypeOne

    Scenario: Get null TargetType
        Given a REST client authenticated as accountOne
        When send GET TargetType=NULL
        Then the response status is 404

    Scenario: Update TargetType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tTypeTwo |
        And send PUT TargetType=tTypeOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET TargetType=tTypeTwo
        Then the response status is 200
        And property name contains tTypeTwo

    Scenario: Update one field of TargetType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tTypeThree |
        And send PATCH TargetType=tTypeOne
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET TargetType=tTypeThree
        Then the response status is 200
        And property name contains tTypeThree

    Scenario: Delete TargetType
        Given a REST client authenticated as admin
        When send DELETE TargetType=tTypeOne
        Then the response status is 204
