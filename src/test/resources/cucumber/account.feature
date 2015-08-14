@account
Feature: Account Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name  | teamOne        |
        And send POST /team
        And a REST client
        When request json body has:
            | name  | one                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send POST /account

    Scenario: Create Account
        Then the response status is 201

    Scenario: Create duplicated Account
        Given a REST client
        When request json body has:
            | name  | one                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 409

    Scenario: Get Account
        Given a REST client
        When send GET /account/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Account
        Given a REST client
        When send GET /account/2
        Then the response status is 404

    Scenario: Update Account name
        Given a REST client
        When request json body has:
            | name  | two                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 204
        And a REST client
        When send GET /account/1
        Then the response status is 200
        And property name contains two

    Scenario: Update Account email
        Given a REST client
        When request json body has:
            | name  | one                         |
            | email | other@teste.com             |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 204
        And a REST client
        When send GET /account/1
        Then the response status is 200
        And property email contains other@teste.com

    Scenario: Update one field of Account
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /account/1
        Then the response status is 204
        And a REST client
        When send GET /account/1
        Then the response status is 200
        And property name contains two

    Scenario: Delete Account
        Given a REST client
        When send DELETE /account/1
        Then the response status is 204
        And  a REST client
        When send GET /account/1
        Then the response status is 404
