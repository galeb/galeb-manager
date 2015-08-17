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
        And a REST client
        When request json body has:
            | name  | other                       |
            | email | other@teste.com             |
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
        When send GET /account/3
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

    Scenario: Create Account as its owner
        Given a REST client authenticated as two
        When request json body has:
            | name  | two                         |
            | email | test2@teste.com             |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 401

    Scenario: Get Account as its owner
        Given a REST client authenticated as one
        When send GET /account/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Account as its owner
        Given a REST client authenticated as one
        When send GET /account/3
        Then the response status is 404

    Scenario: Update Account name as its owner is not permitted
        Given a REST client authenticated as one
        When request json body has:
            | name  | two                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 403
        And a REST client
        When send GET /account/1
        Then the response status is 200
        And property name contains one

    Scenario: Update Account email as its owner
        Given a REST client authenticated as one
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

    Scenario: Update field name of Account as its owner is not permitted
        Given a REST client authenticated as one
        When request json body has:
            | name | two |
        And send PATCH /account/1
        Then the response status is 403
        And a REST client
        When send GET /account/1
        Then the response status is 200
        And property name contains one

    Scenario: Delete Account as its owner
        Given a REST client authenticated as one
        When send DELETE /account/1
        Then the response status is 204
        And  a REST client
        When send GET /account/1
        Then the response status is 404

    Scenario: Create Account as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name  | two                         |
            | email | test2@teste.com             |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 401

    Scenario: Get Account as anonymous is not permitted
        Given a REST client unauthenticated
        When send GET /account/1
        Then the response status is 401

    Scenario: Update Account name as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name  | two                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 401

    Scenario: Update Account email as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name  | one                         |
            | email | other@teste.com             |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 401

    Scenario: Update field name of Account as anonymous is not permitted
        Given a REST client unauthenticated
        When request json body has:
            | name | two |
        And send PATCH /account/1
        Then the response status is 401

    Scenario: Delete Account as anonymous is not permitted
        Given a REST client unauthenticated
        When send DELETE /account/1
        Then the response status is 401

    Scenario: Get Account as other account is not permitted
        Given a REST client authenticated as other
        When send GET /account/1
        Then the response status is 404

    Scenario: Update Account name as other account is not permitted
        Given a REST client authenticated as other
        When request json body has:
            | name  | two                         |
            | email | test@teste.com              |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 403

    Scenario: Update Account email as other account is not permitted
        Given a REST client authenticated as other
        When request json body has:
            | name  | one                         |
            | email | other@teste.com             |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send PUT /account/1
        Then the response status is 403

    Scenario: Update field name of Account as other account is not permitted
        Given a REST client authenticated as other
        When request json body has:
            | name | two |
        And send PATCH /account/1
        Then the response status is 403

    Scenario: Delete Account as other account is not permitted
        Given a REST client authenticated as other
        When send DELETE /account/1
        Then the response status is 404

