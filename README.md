# Request Message Body Length Limit Filter

Servlet Filter that limits the size of the http request message body.
Rejects a Http request (with status code 413) if its body is biger then the one specified as param.

#### Filter Parameters:
- **maxMessageBodyLength** - maximal number of bytes of a http request. (default is 52428800 (50MB))
- **allowChunkedTransfer** - true/false - chunked transfer can be explicitly rejected (with status code 411), since you cannot determine the length of the request. If the chunked request is allowed, it still counts the bytes from the request, and applies the maxMessageBodyLength constraint.

#### Additional Behaviour:
- If the Content-Length header is not present in (non chunked) request, its automatically rejected (411).
- It applies only on http methods: POST, PUT, OPTIONS, DELETE, PATCH. Other methods are ignored

