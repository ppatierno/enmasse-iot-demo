package com.redhat.iot;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;

import java.io.IOException;

/**
 * Created by ppatiern on 31/05/17.
 */
public class AMQPReceiver {

    private static String host = "localhost";
    private static int port = 5672;
    private static String address = "max";

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

        Vertx vertx = Vertx.vertx();

        ProtonClient client = ProtonClient.create(vertx);

        client.connect(host, port, done -> {

            if (done.succeeded()) {

                connection = done.result();
                connection.open();

                receiver = connection.createReceiver(address);

                receiver.handler((delivery, message) -> {

                    Section section = message.getBody();

                    if (section instanceof AmqpValue) {

                        String text = (String) ((AmqpValue)section).getValue();
                        System.out.println("Message received " + text);
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
