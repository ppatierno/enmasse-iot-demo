# EnMasse - Spark Streaming demo

This demo shows how it's possible to use a [Kubernetes](https://kubernetes.io/) or [OpenShift](https://www.openshift.com/) cluster running [EnMasse](https://enmasseproject.github.io/) and [Apache Spark](https://spark.apache.org/ in order to have a way for ingesting data through a scalable messaging infrastructure and then processing such data using Spark Streaming jobs.
The proposed IoT scenario is related to an AMQP publisher which sends simulated temperature values and a Spark Streaming job which processes such data in order to get the max value in the latest 5 seconds.

## Prerequisites

The main prerequisite is to have a Kubernetes or OpenShift cluster up and running for deploying EnMasse and the Apache Spark cluster. 

The Kubernetes cluster can be set up locally using [minikube](https://github.com/kubernetes/minikube) project or something like [Azure Container Service](https://azure.microsoft.com/en-us/services/container-service/) for example.

The OpenShift cluster can be set up locally using the `oc` tool as described in the following [guide](https://github.com/openshift/origin/blob/master/docs/cluster_up_down.md).

## Apache Spark

The current repo provides the following resources (in the `spark` directory) for deploying an Apache Spark cluster in _standalone mode_ made of one master node and one worker node.

* _Dockerfile_ : a Docker file for building an Apache Spark image
* _start-common.sh, start-master.sh, start-worker.sh_ : some bash files for starting the master and the worker node
* _spark-master.yaml, spark-master-service.yaml_ : YAML resources files which describe deployment and service for the master node to deploy on Kubernetes
* _spark-worker.yaml_ : YAML resources file which describes deployment for the worker node to deploy on Kubernetes

The above deployment files refer to the ppatierno/spark:2.0 image available on Docker Hub but you can re-build such image using the Dockerfile and then changing the image name in the related deployment files.

> Credits to [this](https://github.com/phatak-dev/kubernetes-spark) project and [this](https://github.com/kubernetes/kubernetes/tree/master/examples/spark) project for definining the above Dockerfile and YAML resources file for deploying Apache Spark on Kubernetes.

## Kubernetes

### Local minikube

TODO

### Azure Container Service

TODO

## OpenShift

TODO

## Demo application

The demo application is provided by this [AMQP Spark Streaming demo](https://github.com/redhat-iot/amqp-spark-demo) repo where the scenario is the following :

* an AMQP publisher application sends simulated temperature values every seconds to the _temperature_ address
* an AMQP Spark driver application uses the [AMQP Spark Streaming connector](https://github.com/radanalyticsio/streaming-amqp) in order to receive such values from the same _temperature_ address and then executing a Spark job for getting max value in the latest 5 seconds

The above repo describes all the steps for setting up the demo running against an Apache Artemis broker instance using a queue named _temperature_ as destination.

For the current demo, the applications (publisher and Spark driver) are used in a different way with the _temperature_ address deployed as an anycast address in the EnMasse messaging infrastructure (so without the need for a broker). It means that there is no "store and forward" mechanism involved but "direct messaging" : only when the Spark driver application with the AMQP receiver is up and running, the publisher gets credits for start sending messages processed in real time by the Spark Streaming job itself.

The first needed step is to build the source code application as described [here](https://github.com/redhat-iot/amqp-spark-demo#building-the-demo-source-code).

In order to run both applications the steps needed are described [here](https://github.com/redhat-iot/amqp-spark-demo#running-demo-applications).
Regarding the AMQP publisher application, the _messaging_ service address/port will be specified; the same of the Spark driver other than the right address for the Spark master node.


