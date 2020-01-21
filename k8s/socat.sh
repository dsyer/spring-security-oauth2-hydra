#!/bin/bash

function socat-expose() {

    service=$1
    local_port=$2
    service_port=$( [ $# -gt 2 ] && echo $3 || echo $2 )
    node_port=$(kubectl get service $service -o=jsonpath="{.spec.ports[?(@.port == ${service_port})].nodePort}")

    if [ "$?" != "0" ]; then
    
        echo "No such service (${service}) on port=${service_port}"
    
    else

        if docker ps | grep -q kind-proxy-${local_port}; then

            echo Port ${local_port} already exposed

        else

            docker run -d --name kind-proxy-${local_port} \
                --publish 127.0.0.1:${local_port}:${service_port} \
                --link kind-control-plane:target \
                alpine/socat -dd \
                tcp-listen:${service_port},fork,reuseaddr tcp-connect:target:${node_port}
        
        fi

    fi
}

socat-expose ui 8080 8080
socat-expose hydra 4444 4444
socat-expose hydra 4445 4445
socat-expose hydra 3000 3000