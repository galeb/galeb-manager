Feature: Farm Support
    The manager have than
    to support REST standard

    Background:
        Given a REST client
        When request json body has:
            | name | EnvOne |
        And send POST /environment
        And a REST client
        When request json body has:
            | name | providerOne |
        And send POST /provider
        And a REST client
        When request json body has:
            | name        | one                            |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm

    Scenario: Create Farm
        Then the response status is 201
        And property name contains one

    Scenario: Create duplicated Environment
        Given a REST client
        When request json body has:
            | name        | one                            |
            | domain      | domain                         |
            | api         | api                            |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send POST /farm
        Then the response status is 409

    Scenario: Get Farm
        Given a REST client
        When send GET /farm/1
        Then the response status is 200
        And property name contains one

    Scenario: Get null Farm
        Given a REST client
        When send GET /farm/2
        Then the response status is 404

    Scenario: Update Farm
        Given a REST client
        When request json body has:
            | name        | two                            |
            | domain      | domain2                        |
            | api         | api2                           |
            | environment | http://localhost/environment/1 |
            | provider    | http://localhost/provider/1    |
        And send PUT /farm/1
        Then the response status is 200
        And property name contains two

    Scenario: Update one field of Environment
        Given a REST client
        When request json body has:
            | name | two |
        And send PATCH /farm/1
        Then the response status is 200
        And property name contains two
        And property domain contains domain

    Scenario: Delete Environment
        Given a REST client
        When send DELETE /farm/1
        Then the response status is 204
