# valdate-schema
an api + html gui for validating yodata-realestate event objects in JSON format.

## setup

install dependencies from the project root directory

```zsh
  $ npm install
```

## sandbox mode

For development and testing you can run the api in sandox mode.

```zsh
  $ npm run dev
```

## api parameters
```http
  POST / HTTP/1.1
  Content-Type: application/json
  Host: localhost:3333

  {
    "object": {
        "topic": "realestate/franchise#transactionreport",
        "data": {...}
    }
  }


  HTTP/1.1 200 OK
  Connection: keep-alive
  Content-Length: 1115
  Date: Thu, 06 Oct 2022 02:55:18 GMT
  Keep-Alive: timeout=5
  content-type: application/json

  {
    "isValid": false,
    "error": {
        "message": "data should have required property 'source'",
        "items": [
            {
                "keyword": "required",
                "dataPath": "",
                "schemaPath": "#/allOf/0/required",
                "params": {
                    "missingProperty": "source"
                },
                "message": "should have required property 'source'"
            }
        ]
    }
}

```
