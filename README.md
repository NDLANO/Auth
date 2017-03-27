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

