**Galeb Manager API** is Spring HATEAOS based, so it's a Hypermedia-Driven RESTful Web Service.

A core principle of HATEOAS is that resources should be discoverable through the publication of links that point to the available resources. There are a few competing de-facto standards of how to represent links in *JSON*. By default, **Galeb Manager API** uses HAL to render responses. HAL defines links to be contained in a property of the returned document.

Example:
```
{
    id: 1,
    name: "Null Environment",
    _created_by: "admin",
    _lastmodified_by: "admin",
    properties: { },
    hash: 0,
    _version: 0,
    _created_at: "2015-10-30T17:03:11.000+0000",
    _lastmodified_at: "2015-10-30T17:03:11.000+0000",
    _status: "OK",
    _links: {
        self: {
            href: "https://api.galeb.globoi.com/environment/1"
        },
        targets: {
            href: "https://api.galeb.globoi.com/environment/1/targets"
        },
        farms: {
            href: "https://api.galeb.globoi.com/environment/1/farms"
        },
        virtualhosts: {
            href: "https://api.galeb.globoi.com/environment/1/virtualhosts"
        }
    }
}
```

IMPORTANT: Attributes started with '_' and the attribute 'id' are read-only and internally defined.

# Common attributes

| attribute   | required | description        | 
| ------------|----------|--------------------|
| name        | true     | entity name        |
| properties  | false    | properties map     |
| description | false    | entity description |

