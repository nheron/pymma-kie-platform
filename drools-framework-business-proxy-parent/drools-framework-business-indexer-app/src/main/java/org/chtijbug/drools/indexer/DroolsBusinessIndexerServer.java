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
package org.chtijbug.drools.indexer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.chtijbug.drools.ChtijbugObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

// CHECKSTYLE:OFF
@SpringBootApplication
@Configuration
@EnableMongoRepositories(basePackages = "org.chtijbug.drools.proxy.persistence.repository")
@EnableKafka
public class DroolsBusinessIndexerServer {
    public final static String LOGING_TOPIC ="logging";
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

    @Value(value = "${kafka.index.groupid})")
    private String groupID;

    public ConsumerFactory<String, ChtijbugObjectRequest> greetingConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        if (activateSsl) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, this.sslTruststoreLocation);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, this.sslTruststorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, this.sslKeyPassword);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, this.sslKeystorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, this.sslKeystoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, this.sslKeystoreType);
        }
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ChtijbugObjectRequest.class));
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChtijbugObjectRequest>
    ruleKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, ChtijbugObjectRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(greetingConsumerFactory());
        return factory;
    }


    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        System.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "41943040");
        SpringApplication.run(DroolsBusinessIndexerServer.class, args);
    }

}
// CHECKSTYLE:ON
