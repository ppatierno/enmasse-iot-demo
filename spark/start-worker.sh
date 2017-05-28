#!/bin/sh

. /start-common.sh

${SPARK_HOME}/sbin/start-slave.sh spark://spark-master:7077
