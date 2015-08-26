@balancepolicytype
Feature: BalancePolicyType Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name  | teamOne |
        And send POST /team
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name     | accountOne                  |
            | password | password                    |
            | email    | test@test.com               |
            | roles    | [ ROLE_USER ]               |
            | teams    | [ http://localhost/team/1 ] |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyOne |
        And send POST /balancepolicytype

    Scenario: Create BalancePolicyType
        Then the response status is 201

    Scenario: Create duplicated BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyOne |
        And send POST /balancepolicytype
        Then the response status is 409

    Scenario: Get BalancePolicyType
        Given a REST client authenticated as accountOne
        When send GET /balancepolicytype/1
        Then the response status is 200
        And property name contains tBalancePolicyOne

    Scenario: Get null BalancePolicyType
        Given a REST client authenticated as accountOne
        When send GET /balancepolicytype/2
        Then the response status is 404

    Scenario: Update BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyTwo |
        And send PUT /balancepolicytype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /balancepolicytype/1
        Then the response status is 200
        And property name contains tBalancePolicyTwo

    Scenario: Update one field of BalancePolicyType
        Given a REST client authenticated as admin
        When request json body has:
            | name | tBalancePolicyThree |
        And send PATCH /balancepolicytype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /balancepolicytype/1
        Then the response status is 200
        And property name contains tBalancePolicyThree

    Scenario: Delete BalancePolicyType
        Given a REST client authenticated as admin
        When send DELETE /balancepolicytype/1
        Then the response status is 204
        And a REST client authenticated as admin
        When send GET /balancepolicytype/1
        Then the response status is 404