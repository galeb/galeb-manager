@virtualhostWithRule
Feature: Virtualhost with Rule Support
    The manager have than to support REST standard
    and constraints are applied

        Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | oneEnv |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as admin
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
            | name  | projOne                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name  | projTwo                     |
            | teams | [ http://localhost/team/1 ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | providerOne |
        And send POST /provider
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name        | farmOne                        |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | one                            |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | urlPath |
        And send POST /ruletype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | cookie |
        And send POST /ruletype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | poolOne |
        And send POST /targettype
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | targetOne                      |
            | environment | http://localhost/environment/1 |
            | targetType  | http://localhost/targettype/1  |
            | project     | http://localhost/project/1     |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name         | ruleOne                            |
            | ruleType     | http://localhost/ruletype/1        |
            | virtualhosts | [ http://localhost/virtualhost/1 ] |
            | target       | http://localhost/target/1          |
        And send POST /rule
        Then the response status is 201

    Scenario: Delete VirtualHost with Rule not allowed
        Given a REST client authenticated as accountOne
        When send DELETE /virtualhost/1
        Then the response status is 409

    Scenario: Delete VirtualHost, deleting Rule before, is allowed
        Given a REST client authenticated as accountOne
        When send DELETE /rule/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send DELETE /virtualhost/1
        Then the response status is 204