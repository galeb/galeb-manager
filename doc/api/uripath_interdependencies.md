# URI Path & Interdepedencies

## URI paths

| Info                                               | path               | admin view only | admin write only |
|----------------------------------------------------|--------------------|:---------------:|:----------------:|
| URI base (show all resources)                       | /                  | ---             | ---              |
| Projects                                           | /project           | false           | false            |
| Targets (backends, reals, etc)                     | /target            | false           | false            |
| Pools (backends pools, other resources pools)      | /pool              | false           | false            |
| VirtualHosts (virtual servers, Host header filter) | /virtualhost       | false           | false            |
| Rules (L7 rules)                                   | /rule              | false           | false            |
| Rule Types                                         | /ruletype          | false           | false            |
| Balance Policy Types                               | /balancepolicytype | false           | true             |
| Balance Policies                                   | /balancepolicy     | false           | true             |
| Teams (user groups)                                | /team              | false           | true             |
| Accounts (users, logins)                           | /account           | false           | true             |
| Farms (Galeb Router cluster farm)                  | /farm              | true            | true             |
| Provider (drivers, provisioning, etc)              | /provider          | true            | true             |
| Environment                                        | /environment       | false           | true             |

## Special URI paths

| info                                                |                    | admin view only | admin write only |
|-----------------------------------------------------|--------------------|:---------------:|:----------------:|
| Search resources (XXXX = resource name)             | /XXXX/search       | false           | ---              |
| Search resource path (X = resource name, Y = query) | /X/search/Y        | false           | ---              |
| Token (self authentication header token - if logged)| /token             | false           | ---              |
| Json Schema (XXXX = internal class name)            | /schema/XXXX       | true            | ---              |
| ALPS Schema                                         | /alps              | false           | ---              |
| Reload Farm (forced)                                | /reload/<farm id>  | true            | true             |

## Resources dependencies

| Resource            | depends                              |
|---------------------|--------------------------------------|
| Project             | Team                                 |
| Target              | Project, Environment, Pool           |
| Pool                | Project, Environment, Balance Policy |
| VirtualHost         | Project, Environment                 |
| Rule                | Rule Type, Pool                      |
| Rule Type           |                   ---                |
| Balance Policy Type |                   ---                |
| Balance Policy      | Balance Policy Type                  |
| Team                |                   ---                |
| Account             | Team                                 |
| Farm                | Provider, Environment                |
| Provider            |                   ---                |
| Environment         |                   ---                |
