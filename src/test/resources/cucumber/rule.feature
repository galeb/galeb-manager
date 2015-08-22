@rule
Feature: Rule Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | urlPath |
        And send POST /ruletype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | poolOne |
        And send POST /targettype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | envOne |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as admin
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
            | name        | virtOne                        |
            | environment | http://localhost/environment/1 |
            | project     | http://localhost/project/1     |
        And send POST /virtualhost
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
            | name     | ruleOne                        |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
            | target   | http://localhost/target/1      |
        And send POST /rule

    Scenario: Create Rule
        Then the response status is 201

    Scenario: Create duplicated Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name     | ruleOne                        |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
            | target   | http://localhost/target/1      |
        And send POST /rule
        Then the response status is 409

    Scenario: Create Rule with parent and target in different Farms
        Given a REST client authenticated as admin
        When request json body has:
            | name | envTwo |
        And send POST /environment
        And a REST client authenticated as admin
        When request json body has:
            | name        | farmTwo                        |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/2 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm
        And a REST client authenticated as accountOne
        When request json body has:
            | name        | targetTwo                      |
            | environment | http://localhost/environment/2 |
            | targetType  | http://localhost/targettype/1  |
            | project     | http://localhost/project/1     |
        And send POST /target
        And a REST client authenticated as accountOne
        And request json body has:
            | name     | ruleTwo                        |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
            | target   | http://localhost/target/2      |
        And send POST /rule
        Then the response status is 400

    Scenario: Get Rule
        Given a REST client authenticated as accountOne
        When send GET /rule/1
        Then the response status is 200
        And property name contains ruleOne

    Scenario: Get null Rule
        Given a REST client authenticated as accountOne
        When send GET /rule/2
        Then the response status is 404

    Scenario: Update Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name     | ruleTwo                        |
            | ruleType | http://localhost/ruletype/1    |
            | parent   | http://localhost/virtualhost/1 |
            | target   | http://localhost/target/1      |
        And send PUT /rule/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /rule/1
        Then the response status is 200
        And property name contains ruleTwo

    Scenario: Update one field of Rule
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | ruleThree |
        And send PATCH /rule/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /rule/1
        Then the response status is 200
        And property name contains ruleThree

    Scenario: Delete Rule
        Given a REST client authenticated as accountOne
        When send DELETE /rule/1
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /rule/1
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET /ruletype/1
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET /virtualhost/1
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET /target/1
        Then the response status is 200