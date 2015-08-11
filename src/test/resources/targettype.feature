Feature: TargetType Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype

    Scenario: Create TargetType
        Then the response status is 201
        And property name contains tTypeOne

    Scenario: Create duplicated TargetType
        Given a REST client
        When request json body has:
            | name | tTypeOne |
        And send POST /targettype
        Then the response status is 409

    Scenario: Get TargetType
        Given a REST client
        When send GET /targettype/1
        Then the response status is 200
        And property name contains tTypeOne

    Scenario: Get null TargetType
        Given a REST client
        When send GET /targettype/2
        Then the response status is 404

    Scenario: Update TargetType
        Given a REST client
        When request json body has:
            | name | tTypeTwo |
        And send PUT /targettype/1
        Then the response status is 200
        And property name contains tTypeTwo

    Scenario: Update one field of TargetType
        Given a REST client
        When request json body has:
            | name | tTypeTree |
        And send PATCH /targettype/1
        Then the response status is 200
        And property name contains tTypeTree

    Scenario: Delete TargetType
        Given a REST client
        When send DELETE /targettype/1
        Then the response status is 204
