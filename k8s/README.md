# Deployment to K8S

Follows a guide on how to deploy the application on a local Kubernetes cluster.

# Requirements

- A running local Kubernetes cluster. If you are using macOS follow this [guide](https://docs.docker.com/docker-for-mac/#kubernetes).
- The docker image `bphenriques/employee-shifts-api:0.9.0` but be present in your docker registry.

# Deploying

Deploy everything:
```sh
$ make deploy
```

After the deployment is done, you can check the status of the existing pods:
```sh
$ kubectl get deployments
NAME                  READY   UP-TO-DATE   AVAILABLE   AGE
employee-shifts-api   2/2     2            2           115s
postgres              1/1     1            1           115s
```

Once you see this the status `Running` and `READY` set to `2/2` you may use the API:
- **API-Docs**: http://localhost:30001/swagger-ui.html
- **Readiness probe**: http://localhost:30002/actuator/health/readiness
- **Liveness probe**: http://localhost:30002/actuator/health/liveness
- **Prometheus scrape**: http://localhost:30002/actuator/prometheus

Note: The port `30002` is exposed outside solely for demonstration purposes. It should be hidden in other environments.

Do some requests and check the logs:
```sh
$ kubectl logs -f employee-shifts-api-b46ff4745-jwl44
```

Once done, use the following command to tear everything down:
```sh
$ make delete
```

# Future Work

This folder serves as Kubernetes deployment proof-of-concept. There is lots of things that should be improved:
- Rolling Updates.
- Secrets management.
- Security (Postgres specially).
- Isolation

# Reference pages:

Follows some links I found useful:
- Kubernetes concepts: https://kubernetes.io/docs/concepts/
- Kubernetes 101: https://kubernetes.io/docs/concepts/overview/
- Docker + Kubernetes: https://docs.docker.com/get-started/kube-deploy/
- Deploy Postgres: https://severalnines.com/database-blog/using-kubernetes-deploy-postgresql
- For manual operations when things to wrong: https://kubernetes.io/docs/concepts/workloads/pods/disruptions/
- Changing how the application is accessed externally:: https://kubernetes.io/docs/concepts/services-networking/ingress/
- Rolling updates: https://www.bluematador.com/blog/kubernetes-deployments-rolling-update-configuration
- Managing secrets: https://medium.com/@harsh.manvar111/kubernetes-secret-vs-vault-fb57d75ce553
- Managing secrets using Hashi Vault: https://www.vaultproject.io/

There are more links, I added in-line in some files.
