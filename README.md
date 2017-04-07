# Auth #
[![Build Status](https://travis-ci.org/NDLANO/auth.svg?branch=master)](https://travis-ci.org/NDLANO/auth)

## Build & Run ##

```sh
$ cd Auth
$ ./sbt
> container:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## About headers
When a request is forwarded from Kong it adds a few headers when the app-key has been verified OK.

1. X-Consumer-ID: The ID of the Consumer on Kong
2. X-Consumer-Custom-ID: The custom_id of the Consumer (if set) --> This is only set on system-consumers (e.g. the learningpath-frontend consumer)
3. X-Consumer-Username: The username of the Consumer (if set) --> This is set on all personal users, but not on system-consumers.

## Getting Access Tokens



Tokens are obtained from the ```/auth/tokens``` endpoint.  You do not ned a client id and secret to get a token. When
calling the api you will need a client id and secret together with the token, contact us for client id and secret configuration. 
[https://tools.ietf.org/html/rfc6749#section-4.4]

You obtain an access token by making a ```POST``` request to:

``` 
https://test.api.ndla.no/auth/tokens
```

The response will look like this:

```
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImtLVEdjQXN3RFZKQ0tZZkxkejJlamZray5saXN0aW5nLmZyb250ZW5kIiwicm9sZXMiOltdfSwiaXNzIjoia0tUR2NBc3dEVkpDS1lmTGR6MmVqZmtrLmxpc3RpbmcuZnJvbnRlbmQiLCJpYXQiOjE0OTE1NjgzMzgsImV4cCI6MTQ5MTU3MTkzOH0.-wtH_tJ7XaHBjclgi4MlwX2NJ7XEU3i9W6LAwIiXTfI",
  "token_type": "bearer",
  "expires_in": 1491571938
}
```

```expires_in``` is an epoch timestamp pluss the valid token time, giving you time of expiraction in epoch time 
[https://en.wikipedia.org/wiki/Unix_time]. 

```access_token``` is a Base64 encodes string, to view the content use e.g. the [https://jwt.io/] service. 
The ```access_token``` value is what you must pass in an Authorization header with your API call in this form:
``` 
Authorization: Bearer {access_token}
```

### Implementation Strategy

We recomend using an approach where you check the ```expires_in``` value to the current time in Epoch time, and 
then on later API calls, check the expires time against the current time to see if you need to fetch a new token. 
In this case, your processing sequence will look like this:
![Illustration get token expiration check](get-token-check-expiration.png?raw=true "Get token expiration check")
