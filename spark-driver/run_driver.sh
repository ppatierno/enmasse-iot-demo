#!/bin/sh

if [ -z "$SPARK_MASTER_SERVICE_HOST" ]; then
    export SPARK_MASTER=local[*]
else
    export SPARK_MASTER=spark://$SPARK_MASTER_SERVICE_HOST:$SPARK_MASTER_SERVICE_PORT
fi

echo "Spark master node = " $SPARK_MASTER

${SPARK_HOME}/bin/spark-submit \
    --class com.redhat.iot.AMQPTemperature \
    --master $SPARK_MASTER \
    /spark-driver-1.0-SNAPSHOT.jar