package org.chtijbug.drools.reverseproxy;


import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.chtijbug.drools.ReverseProxyUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.chtijbug.drools.proxy.persistence.repository")
public class DroolsBusinessReverseProxyServer {

    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Value("${pymma.kafka.activateSsl:false}")
    private boolean activateSsl;

    @Value("${pymma.kafka.sslTruststoreLocation:}")
    private String sslTruststoreLocation;

    @Value("${pymma.kafka.sslTruststorePassword:}")
    private String sslTruststorePassword;

    @Value("${pymma.kafka.sslKeyPassword:}")
    private String sslKeyPassword;

    @Value("${pymma.kafka.sslKeystorePassword:}")
    private String sslKeystorePassword;

    @Value("${pymma.kafka.sslKeystoreLocation:}")
    private String sslKeystoreLocation;

    @Value("${pymma.kafka.sslKeystoreType:}")
    private String sslKeystoreType;

    public ConsumerFactory<String, ReverseProxyUpdate> mappingConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "greeting");
        if (activateSsl) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststoreLocation);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.sslKeystoreType);
        }
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ReverseProxyUpdate.class));
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReverseProxyUpdate>
    mappingKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ReverseProxyUpdate> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mappingConsumerFactory());
        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(DroolsBusinessReverseProxyServer.class, args);
    }

}
