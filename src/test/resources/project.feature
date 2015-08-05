Feature: Project Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /project

    Scenario: Create Project
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Project
        Given a REST client
        When request json body has:
            | name | one |
        And send POST /project
        Then the response status is 409

    Scenario: Get Project
        Given a REST client
        When send GET /project/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Project
        Given a REST client
        When send GET /project/2
        Then the response status is 404

    Scenario: Update Project
        Given a REST client
        When request json body has:
            | name | two |
        And send PUT /project/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Project
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /project/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Project
        Given a REST client
        When send DELETE /project/1
        Then the response status is 204
