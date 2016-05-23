# HTTP Request Message Body Length Limit Filter

Servlet Filter that limits the size of the http request message body.
Rejects a Http request (with status code 413) if its body is biger then the one specified as param.

#### Introduction
The purpose of the filter is to prevent the client to post too large http requests, that can cause some memory issues on server. This is done by checking the Content-Length header of the request (see chunked transfer below). Note that servlet containers like Tomcat, does not limit this, its the responsibility of the application.

#### Filter Parameters:
- **maxMessageBodyLength** - maximal length of message body of http request in bytes. (default is 1048576 (1MB))
- **allowChunkedTransfer** - true/false - (default is false); Chunked transfer can be explicitly rejected (with status code 411), since you cannot determine the length of the request. If the chunked request is allowed, it still counts the bytes from the request, and applies the maxMessageBodyLength constraint.

#### Additional Behaviour:
- If the Content-Length header is not present in (non chunked) request, its automatically rejected (411).
- It applies only on http methods: POST, PUT, OPTIONS, DELETE, PATCH. Other methods are ignored.

#### Notes
- Tomcat has a *maxPostSize* attribute on connectors, but this limits just certain kind of posts, see post on [stack overflow](http://stackoverflow.com/questions/14075287/does-maxpostsize-apply-to-multipart-form-data-file-uploads)