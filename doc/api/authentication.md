# Schemas

* Basic Auth
* Token Auth

# Basic Auth example

```
$ curl https://admin:password@localhost/virtualhost
```

# Token Auth example

```
$ curl -v https://admin:password@localhost/token
....
{
    admin: true,
    account: "admin",
    token: "e1a6eb71-2cda-41a3-96e0-5167ccc4a145"
}
....

$ curl -H'x-auth-token: e1a6eb71-2cda-41a3-96e0-5167ccc4a145' http://localhost/virtualhost
```
