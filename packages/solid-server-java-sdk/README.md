# Solid Server - Java SDK
- [Overview](#overview)
- [Build](#build)
- [Use](#use)

## Overview

This SDK is the fundamental building block for the Solid specification concepts targeted at Server implementations.  
It requires Java 8 or later.

## Build
### Binaries
To build a standalone jar usable in projects:
```bash
./gradlew build
```
Jar is produced in `build/libs/`

### Test
To run the tests:
```bash
./gradlew test
```
Detailed results are produced and saved at `build/reports/tests/test/index.html`

### Javadoc
To produce the javadoc:
```bash
./gradlew javadoc
```
Then go to `build/docs/javadoc/`

## Use
To use in other projects, either build the binary jar and include in your classpath, or import the project directly.

### Gradle
In `settings.gradle`:
```groovy
include(':sdk')
project(':sdk').projectDir = file('/path/to/this/sdk')
```
In `build.gradle`:
```groovy
dependencies {
    api project(':sdk')
    
    // Other dependencies...
}
```

## Structures
### Request
Example:
```json
{
  "id": "2c15e002-3d61-11eb-bdc0-639a366e0573",
  "timestamp": "1970-01-01T00:00:00Z",
  "url": "https://mypod.example.org/inbox/",
  "method": "POST",
  "security": {
    "see": "Security Context structure"
  },
  "target": "https://mypod.example.org/inbox/",
  "destination": "https://mypod.example.org/inbox/318dc6f83d6111ebb446dbcb2c6ea52e",
  "acl": {
    "see": "ACL structure"
  },
  "rawHeaders": {
    "Content-Type": ["application/json"]
  },
  "parameters": {},
  "policy": {
    "default": null,
    "global": null,
    "local": null
  },
  "body": "",
  "isBase64Encoded": true,
  "solidService": false
}
```

Key               | Type             | Description
------------------|------------------|------------------------------------
`id`              | string           | ID of a request, usually as an UUID
`timestamp`       | string           | Date and time in ISO 8601 format
`url`             | string           | Raw URL requested by the client
`method`          | string           | Method requested by the client
`solidService`    | boolean          | If the request was made internally by a service
`security`        | Security Context | Security context for the request. May be null or partial if `solidService` is set to true. Informational only
`target`          | string           | The target resource of the request
`destination`     | string           | The actual destination of the request. Different from `target` if a file ID had to be generated
`acl`             | ACL              | The resolved ACL for the request on which access control is based
`rawHeaders`      | object           | Raw headers as a Key-String array value pair
`parameters`      | object           | Resolved query parameters for the request, as key values pair string values
`policy`          | object           | Policies for the request, as key values string-object pairs
`body`            | string           | If any, the body. May be `null`
`isBase64Encoded` | boolean          | If the body is Base64 encoded

## Response
Example:
```json
{
  "fileId": "https://mypod.example.org/inbox/318dc6f83d6111ebb446dbcb2c6ea52e",
  "status": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "isBase64Encoded": true,
  "body": "e30="
}
```

Key               | Type    | Description
------------------|---------|------------------------------------
`fileId`          | string  | If a new file was created for a given request, the ID of the file
`status`          | integer | The status code returned to the client
`headers`         | object  | Headers for the response as key-value string pairs
`body`            | string  | If any, the body. May be `null`
`isBase64Encoded` | boolean | If the body is Base64 encoded

### Security Context
```json
{
  "agent": "https://agent.example.org/profile/card#me",
  "instrument": "https://instrument.example.org/profile/card#me",
  "isAdmin": false,
  "isDefaultAllowed": false
}
```

Key                | Type    | Description
-------------------|---------|------------------------------------
`agent`            | string  | The related agent. May be null
`instrument`       | string  | The related instrument. May be null if anonymous
`isAdmin`          | boolean | If the identified agent/instrument has admin rights
`isDefaultAllowed` | boolean | If the identified agent/instrument has the Default Allowed flag

The `Default Allowed` flag is implementation specific and may grant any kind of default access to containers or
resources even if no ACL was found.

### ACL
```json
{
  "modes": ["Append"],
  "scope": null
}
```

Key                | Type    | Description
-------------------|---------|------------------------------------
`modes` | array[string] | Modes granted by the ACL for this request
`scope` | array[string] | Scope array found in the ACL, if any

### Storage event
When a write/delete operation is performed on a resource, a storage event is sent to the event bus.
Example:
```json
{
  "type": "AddAction",
  "id": "https://mypod.example.org/inbox/318dc6f83d6111ebb446dbcb2c6ea52e",
  "target": "https://mypod.example.org/inbox/",
  "request": {
    "see": "Request structure"
  },
  "object": {
    "hello": "world"
  },
  "response": {
    "see": "Response structure" 
  }
}
```

Key        | Type     | Description
-----------|----------|----------------------------------
`type`     | string   | `AddAction` when a new file is created<br>`UpdateAction` when the content of an existing file is updated<br>`DeleteAction` when a file is deleted
`id`       | string   | ID of the event, can be the URI of the related file
`target`   | string   | The resource the event relates to
`request`  | Request  | The request that triggered this event, as a Request structure
`object`   | object   | If the request body is JSON, the content of it. May not always be present.
`response` | Response | The response to the request as a Response structure. Optional.
