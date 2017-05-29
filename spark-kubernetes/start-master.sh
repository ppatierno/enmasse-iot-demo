#!/bin/sh

. /start-common.sh

echo "$(hostname -i) spark-master" >> /etc/hosts

${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.master.Master --ip spark-master --port 7077 --webui-port 8080
