Feature: Target Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /targettype
        And the response status is 201
        And a REST client
        And request json body has:
            | name       | one                           |
            | targetType | http://localhost/targettype/1 |
        And send POST /target

    Scenario: Create Target
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Target
        Then the response status is 201
        And a REST client
        When request json body has:
            | name | one |
            | targetType | http://localhost/targettype/1 |
        And send POST /target
        Then the response status is 409

    Scenario: Get Target
        Then the response status is 201
        And a REST client
        When send GET /target/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Target
        Given a REST client
        When send GET /target/2
        Then the response status is 404

    Scenario: Update Target
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name | two |
        And send PUT /target/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Target
        Then the response status is 201
        And property name contains one
        And a REST client
        When request json body has:
            | name | two |
        And send PATCH /target/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Target
        Then the response status is 201
        And property name contains one
        And a REST client
        When send DELETE /target/1
        Then the response status is 204
