#!/bin/bash

echo "Deploying Apache Spark ..."
kubectl create -f ../spark-kubernetes/spark-master.yaml -n enmasse-spark
kubectl create -f ../spark-kubernetes/spark-master-service.yaml -n enmasse-spark

kubectl create -f ../spark-kubernetes/spark-worker.yaml -n enmasse-spark
echo "... Apache Spark deployed"