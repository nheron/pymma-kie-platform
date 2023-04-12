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

import org.apache.activemq.ActiveMQXAConnectionFactory;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsTemplate;
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
@EnableJpaRepositories(basePackages = "org.chtijbug.drools.proxy.persistence.repository")

public class DroolsBusinessIndexerServer {
    public final static String LOGING_TOPIC ="logging";

    @Value("${almady.jms.url}")
    private String jmsUrl;
    private JmsTemplate jmsTemplate;
    private ActiveMQXAConnectionFactory connectionFactory;

    @Bean(name = "jmsTemplate")
    JmsTemplate createJmsTemplate() {
        connectionFactory = new ActiveMQXAConnectionFactory(jmsUrl);
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setAlwaysSyncSend(true);
        connectionFactory.setProducerWindowSize(1024000);
        jmsTemplate = new JmsTemplate(connectionFactory);
        return jmsTemplate;
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
