package org.chtijbug.drools.console;


import com.vaadin.flow.spring.SpringServlet;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.chtijbug.drools.console.middle.DababaseContentUpdate;
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
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.MultipartConfigElement;

@Configuration
@SpringBootApplication
@EnableJpaRepositories("org.chtijbug.drools.proxy.persistence.repository")
@PropertySource("classpath:application.properties")
@EnableSwagger2
@EnableScheduling
@EnableTransactionManagement
public class DroolsSpringBootConsoleApplication extends SpringBootServletInitializer {


    @Value("${kie-wb.baseurl}")
    private String kiewbUrl;

    @Value("${almady.jms.url}")
    private String jmsUrl;
    private JmsTemplate jmsTemplate;
    private ActiveMQXAConnectionFactory connectionFactory;


    @Autowired
    private DababaseContentUpdate dababaseContentUpdate;
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

    @Bean

    public MultipartConfigElement multipartConfigElement() {

        MultipartConfigFactory factory = new MultipartConfigFactory();
        DataSize dataSize =DataSize.ofMegabytes(100);
        factory.setMaxFileSize(dataSize);
        factory.setMaxRequestSize(dataSize);
        return factory.createMultipartConfig();

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
    public ServletRegistrationBean<SpringServlet> springServlet(ApplicationContext context) {
        return new ServletRegistrationBean<>(new SpringServlet(context), "/admin/*", "/frontend/*");
    }


    public static void main(String[] args) {
        SpringApplication.run(DroolsSpringBootConsoleApplication.class, args);
    }

    @Bean(name = "jmsTemplate")
    JmsTemplate createJmsTemplate() {
        connectionFactory = new ActiveMQXAConnectionFactory(jmsUrl);
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setAlwaysSyncSend(true);
        connectionFactory.setProducerWindowSize(1024000);
        jmsTemplate = new JmsTemplate(connectionFactory);
        return jmsTemplate;
    }

   @EventListener(ApplicationReadyEvent.class)
    public void initPlatform(){
        dababaseContentUpdate.initDatabaseIfNecessary();
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
                .paths(PathSelectors.regex("/api.*"))
                .build()
                .pathMapping("/swagger");
    }



}
