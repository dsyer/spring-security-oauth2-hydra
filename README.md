Spring Boot OAuth2 client and resource server using [Hydra](https://github.com/ory/hydra) OAuth2 provider.

```
$ mvn install
$ docker-compose up
```

Create the OAuth2 client (a bit like in the [Hydra getting started guide](https://www.ory.sh/docs/next/hydra/5min-tutorial)):

```
$ docker-compose exec hydra hydra clients create \
    --endpoint http://127.0.0.1:4445 \
    --id auth-code-client \
    --secret secret \
    --grant-types authorization_code,refresh_token \
    --response-types code,id_token \
    --scope openid,offline \
    --callbacks 'http://127.0.0.1:8080/login/oauth2/code/hydra,http://localhost:8080/login/oauth2/code/hydra'
```

The callbacks allow you to login from "localhost" or "127.0.0.1".

Run the apps locally, e.g. with java on the command line:

```
$ java -jar ui/target/*.jar
$ java -jar resource/target/*.jar
```

or in docker compose:

```
$ docker-compose -f docker-compose.app.yml up
```

Visit `http://localhost:8080` in a browser, login and verify that you can see your access token and the message from the resource server ("Hello foo@bar.com").