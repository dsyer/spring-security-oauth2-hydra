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

## Run Apps Locally

Run the apps locally, e.g. with java on the command line:

```
$ java -jar ui/target/*.jar
$ java -jar resource/target/*.jar
```

## Run Apps in Docker

In docker compose:

```
$ docker-compose -f docker-compose.app.yml up
```

Visit `http://localhost:8080` in a browser, login and verify that you can see your access token and the message from the resource server ("Hello foo@bar.com").

## Deploy to Kubernetes

If all the containers are running in Kubernetes you need to expose the ui (port 8080) and the hydra apps (ports 4444, 3000) so that the browser can see them. The easiest way is probably with a port forward. An alternative would be to use an ingress service. If your cluster is Kind, there is a shell script `k8s/socat.sh` that will expose the ui and hydra apps without the need to set up a forward (and the "tunnel" stays open if the apps restart as well).

You can deploy the whole system:

```
$ kubectl apply -f <(kustomize build k8s/system)
```

or just the hydra bit:

```
$ kubectl apply -f <(kustomize build k8s/hydra)
```

or the individual apps (via `k8s/ui` and `k8s/resource`). If you have the hydra service deployed and it is proxied on localhost on ports 3000 and 4444, then you can also run the apps (ui and resource) on localhost. To set up the OAuth2 client:

```
$ kubectl exec hydra-d94bcb5dd-hms6f -c hydra -ti -- \
        hydra clients create \
        --endpoint http://127.0.0.1:4445 \
        --id auth-code-client \
        --secret secret \
        --grant-types authorization_code,refresh_token \
        --response-types code,id_token \
        --scope openid,offline \
        --callbacks 'http://127.0.0.1:8080/login/oauth2/code/hydra,http://localhost:8080/login/oauth2/code/hydra'
```