Feature: TargetType Support
    The manager have than
    to support REST standard

    Scenario: Create TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 409

    Scenario: Get TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And a REST client
        When send GET /targettype/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null TargetType
        Given a REST client
        When send GET /targettype/2
        Then the response status is 404

    Scenario: Update TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name | two |
        And send PUT /targettype/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name | two |
        And send PATCH /targettype/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete TargetType
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        Then the response status is 201
        And property name contains one
        And a REST client
        When send DELETE /targettype/1
        Then the response status is 204
