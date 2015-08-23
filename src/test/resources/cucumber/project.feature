@project
Feature: Project Support
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
            | name  | accountOne                  |
            | roles | [ ROLE_USER ]               |
            | teams | [ http://localhost/team/1 ] |
            | email | test@fake.com               |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project

    Scenario: Create Project
        Then the response status is 201

    Scenario: Create duplicated Project
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 409

    Scenario: Get Project
        Given a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 200
        And property name contains projOne

    Scenario: Get Project as Team Member
        Given a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 200
        And property name contains projOne

    Scenario: Get null Project
        Given a REST client authenticated as accountOne
        When send GET /project/2
        Then the response status is 404

    Scenario: Update Project
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | projTwo |
        And send PUT /project/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 200
        And property name contains projTwo

    Scenario: Update one field of Project
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | projThree |
        And send PATCH /project/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 200
        And property name contains projThree

    Scenario: Delete Project
        Given a REST client authenticated as accountOne
        When send DELETE /project/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /project/1
        Then the response status is 404