package com.redhat.iot;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonHelper;
import io.vertx.proton.ProtonSender;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.spark.SparkConf;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.amqp.AMQPUtils;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Some;

/**
 * Created by ppatiern on 30/05/17.
 */
public class AMQPTemperature {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPTemperature.class);

    private static final String APP_NAME = "AMQPTemperature";
    private static final Duration BATCH_DURATION = new Duration(1000);

    private static final String CHECKPOINT_DIR = "/tmp/spark-streaming-amqp";

    private static String host = "localhost";
    private static int port = 5672;
    private static String sourceAddress = "temperature";
    private static String destinationAddress = "max";

    public static void main(String[] args) throws InterruptedException {

        String messagingServiceHost = System.getenv("MESSAGING_SERVICE_HOST");
        if (messagingServiceHost != null) {
            host = messagingServiceHost;
        }
        LOG.info("host = {}", host);
        String messagingServicePort = System.getenv("MESSAGING_SERVICE_PORT");
        if (messagingServicePort != null) {
            port = Integer.valueOf(messagingServicePort);
        }
        LOG.info("port = {}", port);

        JavaStreamingContext ssc = JavaStreamingContext.getOrCreate(CHECKPOINT_DIR, AMQPTemperature::createStreamingContext);

        ssc.start();
        ssc.awaitTermination();
    }

    private static JavaStreamingContext createStreamingContext() {

        SparkConf conf = new SparkConf().setAppName(APP_NAME);
        //conf.setMaster("local[2]");
        conf.set("spark.streaming.receiver.writeAheadLog.enable", "true");

        JavaStreamingContext ssc = new JavaStreamingContext(conf, BATCH_DURATION);
        ssc.checkpoint(CHECKPOINT_DIR);

        JavaReceiverInputDStream<Integer> receiveStream =
                AMQPUtils.createStream(ssc, host, port, sourceAddress, message -> {

                    Section body = message.getBody();
                    if (body instanceof AmqpValue) {
                        int temp = Integer.valueOf(((AmqpValue) body).getValue().toString());
                        return new Some<>(temp);
                    } else {
                        return null;
                    }
                }, StorageLevel.MEMORY_ONLY());

        // get maximum temperature in a window
        JavaDStream<Integer> max = receiveStream.reduceByWindow(
                (a,b) -> {

                    if (a > b)
                        return a;
                    else
                        return b;

                }, new Duration(5000), new Duration(5000));

        //max.print();

        Broadcast<String> messagingHost = ssc.sparkContext().broadcast(host);
        Broadcast<Integer> messagingPort = ssc.sparkContext().broadcast(port);

        max.foreachRDD(rdd -> {

            rdd.foreach(record -> {

                Vertx vertx = Vertx.vertx();
                ProtonClient client = ProtonClient.create(vertx);

                LOG.info("Connecting to messaging ...");
                client.connect(messagingHost.value(), messagingPort.getValue(), done -> {

                    if (done.succeeded()) {

                        LOG.info("... connected to {} {}", messagingHost.value(), messagingPort.getValue());

                        ProtonConnection connection = done.result();
                        connection.open();

                        ProtonSender sender = connection.createSender(destinationAddress);
                        sender.open();

                        LOG.info("Sending {} ...", record);
                        Message message = ProtonHelper.message(destinationAddress, record.toString());
                        sender.send(message, delivery -> {

                            LOG.info("... message sent");
                            sender.close();
                            connection.close();
                            vertx.close();
                        });

                    } else {

                        LOG.error("Error on AMQP connection for sending", done.cause());
                    }

                });

            });
        });

        return ssc;
    }

}
