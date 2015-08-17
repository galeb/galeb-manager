@authentication
Feature: Authentication work
    An account should only access to resources
    if authenticated.

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name  | teamOne |
        And send POST /team
        And a REST client authenticated as admin
        When request json body has:
            | name  | teamTwo |
        And send POST /team
        And a REST client authenticated as admin
        When request json body has:
            | name  | accountOne                  |
            | email | accOne@fake.local           |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/1 ] |
        And send POST /account
        And a REST client authenticated as admin
        When request json body has:
            | name  | accountTwo                  |
            | email | accTwo@fake.local           |
            | roles | [ USER ]                    |
            | teams | [ http://localhost/team/2 ] |
        And send POST /account

    Scenario: AccountOne can access your own account
        Given a REST client authenticated as accountOne
        And send GET /account/1
        Then the response status is 200

    Scenario: A nonexistent Account can access a non restricted resource
        Given a REST client authenticated as accountThree
        And send GET /
        Then the response status is 200

    Scenario: A nonexistent Account can not access any restricted resource
        Given a REST client authenticated as accountThree
        And send GET /account/1
        Then the response status is 401

    Scenario: An unauthenticated request can access a non restricted resource
        Given a REST client unauthenticated
        And send GET /
        Then the response status is 200

    Scenario: An unauthenticated request can not access any restricted resource
        Given a REST client unauthenticated
        And send GET /account/1
        Then the response status is 401
