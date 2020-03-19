## Running Locally with Azure AD

There is an OAuth2 client registered in https://portal.azure.com/#blade[Azure AD] with redirects back to localhost. The UI app can always authenticate there if you are connected to the internet and it is running on localhost. The Resource server app needs `spring.profiles.active=azure` to be able to find the right endpoint to inspect the token.

## Running Locally with Okta

There is an OAuth2 client registered in https://developer.okta.com/[Okta] with redirects back to localhost. The UI app can always authenticate there if you are connected to the internet and it is running on localhost. The Resource server app needs `spring.profiles.active=okta` to be able to find the right endpoint to inspect the token. Unfortunately, the way the Okta system works, random users will not have an account in the server. The client is attached to the author's account. You can register yourself in Okta and create an application, using its base URL, id and secret in place of the one in the `application.yml`.

## Run Hydra Locally

Instead of Azure or Okta, you can use [Hydra](https://github.com/ory/hydra) as an OAuth2 provider.

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
$ java -jar consent/target/*.jar
```

The consent app is a Spring Boot app that authenticates user/password and acts as a "consent" service for the Hydra authorization server.

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
$ kubectl apply -f <(kustomize build k8s/hydra/base)
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

## Running Locally With Hydra in the Cloud

There is an instance of Hydra running in GCP at awjaw.crabdance.com (maps to port 4444), bejaw.crabdance.com (maps to port 4445) and cowjaw.crabdance.com (maps to port 3000). It has the `auth-code-client` per the examples above. If you run the apps locally with `spring.profiles.active=crabdance` they will connect to the remote auth server. The YAML needed to deploy the apps is in `k8s/hydra/ingress`. If you want to run them yourself you will need [nginx ingress](https://github.com/kubernetes/ingress-nginx/) as well, plus DNS registrations and certificates for the 3 hosts.

> NOTE: The crabdance instance is maintained by the author, paid for by Pivotal subject to available funding, so it might not always work.
