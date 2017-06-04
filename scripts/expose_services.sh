#!/bin/bash

echo "Exposing EnMasse services ..."
if [ $1 = "acs" ]; then
    kubectl apply -f enmasse-0.11.2/kubernetes/addons/external-lb.yaml -n enmasse-spark
elif [ $1 = "minikube" ]; then
    kubectl patch service messaging -p '{"spec" : { "type" : "NodePort" }}' -n enmasse-spark
    kubectl patch service mqtt -p '{"spec" : { "type" : "NodePort" }}' -n enmasse-spark
else
    echo "Error ! Wrong deployment type !"
fi
echo "... EnMasse services exposed"