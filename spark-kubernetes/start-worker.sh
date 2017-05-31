#!/bin/sh

. /start-common.sh

${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.worker.Worker spark://$SPARK_MASTER_SERVICE_HOST:$SPARK_MASTER_SERVICE_PORT_SPARK --webui-port 8081
