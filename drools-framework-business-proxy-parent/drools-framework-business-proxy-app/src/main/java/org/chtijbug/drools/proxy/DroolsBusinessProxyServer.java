/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.chtijbug.drools.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringSerializer;
import org.chtijbug.drools.ChtijbugObjectRequest;
import org.chtijbug.drools.KieContainerResponse;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

// CHECKSTYLE:OFF
@SpringBootApplication
@Configuration
@EnableKafka
@EnableScheduling
public class DroolsBusinessProxyServer {

    @Value(value = "${org.kie.server.id}")
    private String groupID;

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

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        if (activateSsl) {
            configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            configs.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststoreLocation);
            configs.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
            configs.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
            configs.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
            configs.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystoreLocation);
            configs.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.sslKeystoreType);
        }
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic loggingTopic() {
        return new NewTopic(KafkaTopicConstants.LOGING_TOPIC, 1, (short) 1);
    }



    @Bean
    public ProducerFactory<String, ChtijbugObjectRequest> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        if (activateSsl) {
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststoreLocation);
            configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
            configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
            configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
            configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystoreLocation);
            configProps.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.sslKeystoreType);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public ProducerFactory<String, KieContainerResponse> producerKieContainerResponseactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        if (activateSsl) {
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            configProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststoreLocation);
            configProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
            configProps.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
            configProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
            configProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystoreLocation);
            configProps.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.sslKeystoreType);
        }
        DefaultKafkaProducerFactory<String, KieContainerResponse> producer = new DefaultKafkaProducerFactory<>(configProps);
        producer.transactionCapable();
        producer.setTransactionIdPrefix("trans");
        return producer;
    }

    @Bean(name="deployFinish")
    public NewTopic actionDeployResponseTopic() {
        return new NewTopic(KafkaTopicConstants.RESPONSE_DEPLOY_TOPIC, 1, (short) 1);
    }



    @Bean
    public KafkaTemplate<String, ChtijbugObjectRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        if (System.getProperty("org.kie.server.id") == null) {
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
                String hostName = inetAddress.getHostName();
                System.setProperty("org.kie.server.id", hostName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        System.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "41943040");
        SpringApplication.run(DroolsBusinessProxyServer.class, args);
    }

}
// CHECKSTYLE:ON
