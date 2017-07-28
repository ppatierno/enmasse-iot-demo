#!/bin/bash

echo "Downloading latest EnMasse release ..."
curl -L -O https://github.com/EnMasseProject/enmasse/releases/download/0.11.2/enmasse-0.11.2.tar.gz
tar -xvzf enmasse-0.11.2.tar.gz
echo "... EnMasse downloaded and extracted"

echo "Deploying EnMasse ..."
kubectl create sa enmasse-service-account -n enmasse-spark
kubectl apply -f enmasse-0.11.2/kubernetes/enmasse.yaml -n enmasse-spark

echo "... EnMasse deployed"