# Powerline Server

Server for Powerline App.

## Build

```
$ ./gradlew clean build
```

## Run

```
$ docker-compose up
```

## Authentication

**Facebook**

Send a `POST /signin/facebook?redirect_uri=<app_uri>` request with a browser which will redirect the user to the Faceboon login or approval page. After approval, the user will be redirected back to `<app_uri>` with an authenticated session (represented by a `JSESSIONID` cookie).

Then send a `GET /oauth/authorize?client_id=powerline&response_type=token&scope=basic&redirect_uri=<app_uri>` together with the `JSESSIONID` cookie and it will redirect to `<app_uri>?`