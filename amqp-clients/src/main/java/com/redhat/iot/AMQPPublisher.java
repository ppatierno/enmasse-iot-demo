package com.redhat.iot;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonHelper;
import io.vertx.proton.ProtonSender;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;

import java.util.Random;

/**
 * Created by ppatiern on 31/05/17.
 */
public class AMQPPublisher {

    private static String host = "localhost";
    private static int port = 5672;
    private static String address = "temperature";

    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println("Usage: AMQPPublisher <hostname> <port> <address>");
            System.exit(1);
        }

        host = args[0];
        port = Integer.valueOf(args[1]);
        address = args[2];

        Vertx vertx = Vertx.vertx();

        ProtonClient client = ProtonClient.create(vertx);

        client.connect(host, port, done -> {

            if (done.succeeded()) {

                ProtonConnection connection = done.result();
                connection.open();

                ProtonSender sender = connection.createSender(address);
                sender.open();

                Random random = new Random();

                vertx.setPeriodic(1000, t -> {

                    int temp = 20 + random.nextInt(5);

                    Message message = ProtonHelper.message();
                    message.setBody(new AmqpValue(String.valueOf(temp)));

                    System.out.println("Temperature = " + temp);
                    sender.send(message, delivery -> {

                    });

                });
            }
        });
    }
}
