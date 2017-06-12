# Auth #
[![Build Status](https://travis-ci.org/NDLANO/auth.svg?branch=master)](https://travis-ci.org/NDLANO/auth)

## Getting Access Tokens

Tokens are obtained from the ```/auth/tokens``` endpoint.  Before you can get access tokens, you first need to obtain 
client credentials (a client id and a client secrect) that are specific to the API and operations that you want access to.
To get your client credentials contact us at ndla@knowit.no. 

You obtain an access token by making a ```POST``` request to:

``` 
https://test.api.ndla.no/auth/tokens
```
with the [basic access authentication](https://en.wikipedia.org/wiki/Basic_access_authentication) 
 in the header, 
created by Base64 encoding the string ```clientId:clientSecret``` as the identification part:

``` 
Authorization: Basic QWxhZGRpbjpPcGVuU2VzYW1l
```
and `grant_type=client_credentials` in body.

The response will look like this:

```
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImtLVEdjQXN3T0lVWUtZZkxkejJlamZray55b3VyYXBwLmZyb250ZW5kIiwicm9sZXMiOltdfSwiaXNzIjoia0tUR2NBc3dPSVVZS1lmTGR6MmVqZmtrLnlvdXJhcHAuZnJvbnRlbmQiLCJpYXQiOjE0OTI2ODU0OTAsImV4cCI6MTQ5MjY4OTA5MH0.1vf-bl7CnHHlswxXAUDXyNsANNlCD8dSORoWOnXy7Jg",
  "token_type": "bearer",
  "expires_in": 3600
}
```

```expires_in``` is the lifetime in **seconds** of the access token.

```access_token``` To view the content of the access token use e.g. the https://jwt.io/ service, to check that clientId
and secret are present. 
The ```access_token``` value is what you must pass in an Authorization header with your API call in this form:
``` 
Authorization: Bearer {access_token}
```

### Implementation Strategy

E.g. use an approach where you check the ```expires_in``` value to the current time in Epoch time, and 
then on later API calls, check the expires time against the current time to see if you need to fetch a new token. 
In this case, your processing sequence will look like this:


![Illustration get token expiration check](get-token-check-expiration.png?raw=true "Get token expiration check")
