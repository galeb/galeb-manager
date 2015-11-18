# Collection resource

Collections resources support both GET and POST. All other HTTP methods will cause a 405 Method Not Allowed.

## GET - Returns all entities.

### Supported media types:

* application/hal+json
* application/json

### Parameters:

* page - the page number to access (0 indexed, defaults to 0).
* size - the page size requested (defaults to 20).
* sort - a collection of sort directives in the format ($propertyname,)+[asc|desc]?.

### Example:

```
curl http://localhost/virtualhost?page=0&size=100
```

## POST

Creates a new entity from the given request body.

### Supported media types:

* application/hal+json
* application/json

### Example:

```
curl -XPOST -d '{ "name": "devel" }' http://localhost/environment
```

# Item resource

Item resources support HEAD, GET, PUT, PATCH and DELETE.

## GET

Returns a single entity.

### Supported media types

* application/hal+json
* application/json

## HEAD

Returns whether the item resource is available.

## PUT

Replaces the state of the target resource with the supplied request body.

### Supported media types

* application/hal+json
* application/json

### Example:

```
curl -XPUT -d '{ "name": "production" }' http://localhost/environment/1
```

## PATCH

Similar to PUT but partially updating the resources state.

### Supported media types

* application/hal+json
* application/json
* application/patch+json
* application/merge-patch+json

### Example:

```
curl -XPATCH -d '{ "name": "production" }' http://localhost/environment/1
```

## DELETE

Deletes the resource exposed.

### Example:

```
curl -XDELETE http://localhost/environment/1
```

# The association resource

Galeb Manager API exposes sub-resources of every item resource for each of the associations the item resource has.

## GET

Returns the state of the association resource

### Parameters (if collection path):

* page - the page number to access (0 indexed, defaults to 0).
* size - the page size requested (defaults to 20).
* sort - a collection of sort directives in the format ($propertyname,)+[asc|desc]?.

### Supported media types

* application/hal+json
* application/json

### Example:

```
curl http://localhost/environment/1/farm
curl http://localhost/environment/1/farm/1
```

## PUT

Binds the resource pointed to by the given URI(s) to the resource.

### Supported media types

* text/uri-list

### Example:

```
curl -XPUT -d 'http://localhost/farm/1' http://localhost/environment/1/farm
```

## POST

Only supported for collection associations. Adds a new element to the collection.

### Supported media types

* text/uri-list

### Example:

```
curl -XPOST -d 'http://localhost/farm/1' http://localhost/environment/1/farm
```

## DELETE

Unbinds the association.

### Example:

```
curl -XDELETE http://localhost/environment/1/farm/1
```

# The search resource

As the search resource is a read-only resource it supports GET only.

## GET

Returns a list of links pointing to the individual query method resources

### Parameters:

* page - the page number to access (0 indexed, defaults to 0).
* size - the page size requested (defaults to 20).
* sort - a collection of sort directives in the format ($propertyname,)+[asc|desc]?.

### Example:

```
curl http://localhost/virtualhost/search/findByName?name=test.localdomain&page=0&size=100
```

### Query Methods supported by resource

| Resource            | Query methods                                                                 |
|---------------------|-------------------------------------------------------------------------------|
| Project             | findByName, findByNameContaining                                              |
| Target              | findByName, findByNameContaining, findByFarmId (admin only), findByParentName |
| Pool                | findByName, findByNameContaining, findByFarmId (admin only)                   |
| VirtualHost         | findByName, findByNameContaining, findByFarmId (admin only), getRulesFromVirtualHostName |
| Rule                | findByName, findByNameContaining, findByFarmId (admin only), findByPoolName   |
| Rule Type           | findByName, findByNameContaining                                              |
| Balance Policy Type | findByName, findByNameContaining                                              |
| Balance Policy      | findByName, findByNameContaining                                              |
| Team                | findByName, findByNameContaining                                              |
| Account             | findByName, findByNameContaining                                              |
| Farm                | findByName, findByNameContaining, findByEnvironment                           |
| Provider            | findByName, findByNameContaining                                              |
| Environment         | findByName, findByNameContaining                                              |
