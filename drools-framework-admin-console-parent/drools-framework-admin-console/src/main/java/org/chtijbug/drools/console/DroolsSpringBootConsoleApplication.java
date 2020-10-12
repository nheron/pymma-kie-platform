package org.chtijbug.drools.console;


import com.vaadin.flow.spring.SpringServlet;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.chtijbug.drools.KieContainerResponse;
import org.chtijbug.drools.ReverseProxyUpdate;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.chtijbug.drools.console.middle.DababaseContentInit;
import org.chtijbug.drools.console.service.model.kie.KieConfigurationData;
import org.chtijbug.drools.console.service.util.ApplicationContextProvider;
import org.chtijbug.drools.console.service.wbconnector.KieBusinessCentralConnector;
import org.chtijbug.drools.proxy.persistence.repository.KieWorkbenchRepository;
import org.chtijbug.drools.proxy.persistence.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;
import java.util.Map;

@Configuration
@SpringBootApplication
@EnableMongoRepositories("org.chtijbug.drools.proxy.persistence.repository")
@EnableKafka
@PropertySource("classpath:application.properties")
@EnableSwagger2
@EnableScheduling
public class DroolsSpringBootConsoleApplication extends SpringBootServletInitializer {





    @Value("${kie-wb.baseurl}")
    private String kiewbUrl;

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

    @Autowired
    private DababaseContentInit dababaseContentInit;
    @Autowired
    private KieBusinessCentralConnector kieBusinessCentralConnector;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private KieWorkbenchRepository kieWorkbenchRepository;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "OPTIONS", "DELETE", "PUT")
                        .allowedHeaders("Access-Control-Allow-Origin", "*")
                        .exposedHeaders("Access-Token", "Access-Control-Allow-Origin")
                        .allowCredentials(false).maxAge(3600);
            }
        };
    }


    @Bean(name = "applicationContext")
    public ApplicationContextProvider getAppplicationContext() {
        return new ApplicationContextProvider();
    }

    @Bean
    public KieConfigurationData createKieConfigurationData(){
        KieConfigurationData kieConfigurationData = new KieConfigurationData();
        kieConfigurationData.setKiewbUrl(kiewbUrl);
        kieConfigurationData.setName("demo");
        return kieConfigurationData;
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DroolsSpringBootConsoleApplication.class);
    }


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
        return new NewTopic(KafkaTopicConstants.REVERSE_PROXY, 1, (short) 1);
    }

    @Bean
    public ProducerFactory<String, ReverseProxyUpdate> producerFactory() {
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
        return new DefaultKafkaProducerFactory<>(configProps);
    }


    @Bean
    public KafkaTemplate<String, ReverseProxyUpdate> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }




    public ConsumerFactory<String, KieContainerResponse> greetingConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,"Console");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(KieContainerResponse.class));
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KieContainerResponse>
    ruleKafkaListenerKieContainerUpdateFactory() {

        ConcurrentKafkaListenerContainerFactory<String, KieContainerResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(greetingConsumerFactory());
        return factory;
    }

    @Bean
    public ServletRegistrationBean<SpringServlet> springServlet(ApplicationContext context) {
        return new ServletRegistrationBean<>(new SpringServlet(context), "/admin/*", "/frontend/*");
    }


    public static void main(String[] args) {
        SpringApplication.run(DroolsSpringBootConsoleApplication.class, args);
    }


   @EventListener(ApplicationReadyEvent.class)
    public void initPlatform(){
        dababaseContentInit.initDatabaseIfNecessary();
        /**
       for (KieWorkbench kieWorkbench: kieWorkbenchRepository.findAll()) {
           Map<String, KieContainerResource> kies = new HashMap<>();
           KieServerSetup kieServerSetup = kieBusinessCentralConnector.connectToBusinessCentral("nheron", "adminnheron00@", kieWorkbench.getName(),kieWorkbench.getExternalUrl());
           if (kieServerSetup != null && kieServerSetup.getContainers() != null) {
               for (KieContainerResource kieContainerResource : kieServerSetup.getContainers()) {
                   kies.put(kieContainerResource.getContainerId(), kieContainerResource);
               }
           }
           List<ProjectPersist> projectRepositories = projectRepository.findByKieWorkbench(kieWorkbench);
           if (!projectRepositories.isEmpty()){
               for (ProjectPersist projectPersist : projectRepository.findAll()) {
                   if (projectPersist.getServerNames().size() > 0) {
                       if (!kies.containsKey(projectPersist.getArtifactID()+"_"+projectPersist.getProjectVersion())) {
                           kieBusinessCentralConnector.createContainer("nheron", "adminnheron00@", projectPersist,kieWorkbench.getExternalUrl());
                       } else {
                           kieBusinessCentralConnector.updateContainer("nheron", "adminnheron00@", projectPersist, kies.get(projectPersist.getArtifactID()+"_"+projectPersist.getProjectVersion()),kieWorkbench.getExternalUrl());
                       }
                   }
               }
           }
           /**
               for (ProjectPersist projectPersist : projectRepository.findAll()) {
                   if (projectPersist.getServerNames().size() > 0) {
                       if (!kies.containsKey(projectPersist.getContainerID())) {
                           kieBusinessCentralConnector.createContainer("nheron", "adminnheron00@", projectPersist);
                       } else {
                           kieBusinessCentralConnector.updateContainer("nheron", "adminnheron00@", projectPersist, kies.get(projectPersist.getContainerID()));
                       }
                   }
               }

               ServerInstanceKeyList serverInstanceKeyList = kieBusinessCentralConnector.getListInstances("nheron", "adminnheron00@");
               if (serverInstanceKeyList != null) {
                   for (ServerInstanceKey serverInstanceKey : serverInstanceKeyList.getServerInstanceKeys()) {
                       String serverInstanceId = serverInstanceKey.getServerInstanceId();
                       ContainerList containerList = kieBusinessCentralConnector.getListContainers("nheron", "adminnheron00@", serverInstanceId);
                       System.out.println("coucou");
                       for (Container container : containerList.getContainers()) {

                       }
                   }
                   System.out.println("coucou");
               }
               System.out.println("coucou");

       }
         **/
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.chtijbug.drools.console.restexpose"))
                //.paths(PathSelectors.regex("/api/wb./wb.*"))
                .paths(PathSelectors.regex("/api/wb.*"))
                .build()
                .pathMapping("/swagger");
    }



}
