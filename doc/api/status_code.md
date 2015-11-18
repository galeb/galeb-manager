| Status Code            | info                    |
|------------------------|-------------------------|
| 200 OK                 | for plain GET requests. |
| 201 Created            | for POST and PUT requests that create new resources. |
| 204 No Content         | for PUT, PATCH, HEAD and DELETE requests. |
| 404 Not Found          | if resource not found or invalid path requests. | 
| 405 Method Not Allowed | for other HTTP methods. |
| 400 Bad Request        | if multiple URIs were given for a to-one-association or other invalid request. |
| 5XX Server Error       | internal server error. |
