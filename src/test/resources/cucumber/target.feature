@target
Feature: Target Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client authenticated as admin
        When request json body has:
            | name | targetTypeOne |
        And send POST /targettype
        Then the response status is 201
        And a REST client authenticated as admin
        When request json body has:
            | name | targetTypeTwo |
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
            | name     | accountOne       |
            | password | password         |
            | roles    | [ ROLE_USER ]    |
            | teams    | [ Team=teamOne ] |
            | email    | test@fake.com    |
        And send POST /account
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request json body has:
            | name  | projOne          |
            | teams | [ Team=teamOne ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | targetOne                |
            | targetType  | TargetType=targetTypeOne |
            | environment | Environment=envOne       |
            | project     | Project=projOne          |
        And send POST /target

    Scenario: Create Target
        Then the response status is 201

    Scenario: Create Target with Parent
        Given a REST client authenticated as accountOne
        And request json body has:
            | name       | newTargetTwo              |
            | targetType | TargetType=targetTypeOne  |
            | parents    | [ Target=targetOne ]      |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When send GET /target/2/environment/1
        Then the response status is 200
        And property name contains envOne
        And a REST client authenticated as accountOne
        When send GET /target/2/project/1
        Then the response status is 200
        And property name contains projOne

    Scenario: Create Target with Parent and Project inconsistent
        Given a REST client authenticated as accountOne
        When request json body has:
            | name  | projTwo          |
            | teams | [ Team=teamOne ] |
        And send POST /project
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name       | newTargetTwo              |
            | targetType | TargetType=targetTypeOne  |
            | parents    | [ Target=targetOne ]      |
            | project    | Project=projTwo           |
        And send POST /target
        Then the response status is 400

    Scenario: Create Target with Parent and Environment inconsistent
        Given a REST client authenticated as admin
        When request json body has:
            | name  | envTwo |
        And send POST /environment
        Then the response status is 201
        And a REST client authenticated as accountOne
        And request json body has:
            | name        | newTargetTwo              |
            | targetType  | TargetType=targetTypeOne  |
            | parents     | [ Target=targetOne ]      |
            | environment | Environment=envTwo        |
        And send POST /target
        Then the response status is 400

    Scenario: Create duplicated Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetOne                |
            | targetType  | TargetType=targetTypeOne |
            | environment | Environment=envOne       |
            | project     | Project=projOne          |
        And send POST /target
        Then the response status is 409

    Scenario: Get Target
        Given a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 200
        And property name contains targetOne

    Scenario: Get null Target
        Given a REST client authenticated as accountOne
        When send GET Target=NULL
        Then the response status is 404

    Scenario: Update Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name        | targetOne                |
            | targetType  | TargetType=targetTypeTwo |
            | environment | Environment=envOne       |
            | project     | Project=projOne          |
        And send PUT Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 200
        And property name contains targetOne

    Scenario: Update name field of Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | name | targetOne |
        And send PATCH Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 200
        And property name contains targetOne

    Scenario: Update targetType field of Target
        Given a REST client authenticated as accountOne
        When request json body has:
            | targetType  | TargetType=targetTypeTwo |
        And send PATCH Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 200
        And property name contains targetOne

    Scenario: Delete Target
        Given a REST client authenticated as accountOne
        When send DELETE Target=targetOne
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET Target=targetOne
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET TargetType=targetTypeOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Environment=envOne
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send GET Project=projOne
        Then the response status is 200

    Scenario: Add a new parent target to a target
        Given a REST client authenticated as accountOne
        And request json body has:
            | name        | targetParentOne          |
            | targetType  | TargetType=targetTypeOne |
            | environment | Environment=envOne       |
            | project     | Project=projOne          |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request uri-list body has:
            | Target=targetParentOne |
        And send PATCH /target/1/parents
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1/parents/2
        Then the response status is 200
        And property name contains targetParentOne
        And a REST client authenticated as accountOne
        When send GET /target/2/children/1
        Then the response status is 200
        And property name contains targetOne

    Scenario: Remove a parent target from a target
        Given a REST client authenticated as accountOne
        And request json body has:
            | name        | targetParentOne          |
            | targetType  | TargetType=targetTypeOne |
            | environment | Environment=envOne       |
            | project     | Project=projOne          |
        And send POST /target
        Then the response status is 201
        And a REST client authenticated as accountOne
        When request uri-list body has:
            | Target=targetParentOne |
        And send PATCH /target/1/parents
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1/parents/2
        Then the response status is 200
        And a REST client authenticated as accountOne
        When send DELETE /target/1/parents/2
        Then the response status is 204
        And a REST client authenticated as accountOne
        When send GET /target/1/parents/2
        Then the response status is 404
        And a REST client authenticated as accountOne
        When send GET Target=targetParentOne
        Then the response status is 200
