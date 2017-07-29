package com.redhat.iot;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ppatiern on 29/07/17.
 */
public class KafkaTemperature {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTemperature.class);

    private static final String APP_NAME = "KafkaTemperature";

    private static String sourceAddress = "kafka.temperature";
    private static String destinationAddress = "kafka.max";

    public static void main(String[] args) {

        String bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        LOG.info("KAFKA_BOOTSTRAP_SERVERS = {}", bootstrapServers);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_NAME);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> source = builder.stream(sourceAddress);

        KStream<Windowed<String>, String> max = source
                /*.selectKey((key, value, newKey) -> {
                    return "temp";
                })*/
                .selectKey(new KeyValueMapper<String, String, String>() {
                    @Override
                    public String apply(String key, String value) {
                        return "temp";
                    }
                })
                .groupByKey()
                .reduce((a,b) -> {
                    if (Integer.parseInt(a) > Integer.parseInt(b))
                        return a;
                    else
                        return b;
                }, TimeWindows.of(TimeUnit.SECONDS.toMillis(5000)))
                .toStream();

        WindowedSerializer<String> windowedSerializer = new WindowedSerializer<>(Serdes.String().serializer());
        WindowedDeserializer<String> windowedDeserializer = new WindowedDeserializer<>(Serdes.String().deserializer());
        Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(windowedSerializer, windowedDeserializer);

        // need to override key serde to Windowed<String> type
        max.to(windowedSerde, Serdes.String(), destinationAddress);

        final KafkaStreams streams = new KafkaStreams(builder, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-temperature-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
