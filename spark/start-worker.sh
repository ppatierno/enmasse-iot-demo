#!/bin/sh

. /start-common.sh

${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.worker.Worker spark://spark-master:7077 --webui-port 8081
