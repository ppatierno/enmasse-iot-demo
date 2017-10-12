package com.redhat.iot;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ppatiern on 31/05/17.
 */
public class AMQPReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPReceiver.class);

    private static String host = "localhost";
    private static int port = 5672;
    private static String address = "max";
    private static String username = null;
    private static String password = null;

    private static ProtonConnection connection;
    private static ProtonReceiver receiver;

    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println("Usage: AMQPReceiver <hostname> <port> <address>");
            System.exit(1);
        }

        host = args[0];
        port = Integer.valueOf(args[1]);
        address = args[2];

        if (args.length > 3) {
            username = args[3];
            password = args[4];
        }

        Vertx vertx = Vertx.vertx();

        ProtonClient client = ProtonClient.create(vertx);

        LOG.info("Starting receiver : connecting to [{}:{}] address [{}]", host, port, address);

        client.connect(host, port, username, password, done -> {

            if (done.succeeded()) {

                connection = done.result();
                connection.open();

                LOG.info("Connected as {}", connection.getContainer());

                receiver = connection.createReceiver(address);

                receiver.handler((delivery, message) -> {

                    Section section = message.getBody();

                    if (section instanceof AmqpValue) {
                        String text = (String) ((AmqpValue)section).getValue();
                        LOG.info("Received max = {} °C", text);
                    } else if (section instanceof Data) {
                        Binary data = ((Data)section).getValue();
                        int temp = Integer.valueOf(new String(data.getArray()));
                        LOG.info("Received max = {} °C", temp);
                    } else {
                        LOG.info("Discarded message : body type not supported");
                    }

                    delivery.disposition(Accepted.getInstance(), true);

                }).open();
            }

        });

        try {
            System.in.read();

            if (receiver.isOpen())
                receiver.close();
            connection.close();

            vertx.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
