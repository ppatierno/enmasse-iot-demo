#!/bin/bash

if [ $1 = "acs" ] || [ $1 = "minikube" ]; then

    echo "Deploying demo on ..." $1

    kubectl create namespace enmasse-spark

    bash spark_deploy.sh
    bash enmasse_deploy.sh $1

    echo "... demo deployed"

else
    echo "Error ! Wrong deployment type !"
fi