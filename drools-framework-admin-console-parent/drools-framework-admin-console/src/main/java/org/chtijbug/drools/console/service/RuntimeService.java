package org.chtijbug.drools.console.service;

import org.chtijbug.drools.console.service.model.ReturnPerso;
import org.chtijbug.drools.proxy.persistence.model.ContainerPojoPersist;
import org.chtijbug.drools.proxy.persistence.model.ContainerRuntimePojoPersist;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.repository.ContainerRepository;
import org.chtijbug.drools.proxy.persistence.repository.ContainerRuntimeRepository;
import org.chtijbug.drools.proxy.persistence.repository.RuntimeRepository;
import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;
import org.kie.server.api.model.KieServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@DependsOn("applicationContext")
public class RuntimeService {


    private static Logger logger = LoggerFactory.getLogger(RuntimeService.class);

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private ContainerRuntimeRepository containerRuntimeRepository;

    private RestTemplate restTemplateKiewb = new RestTemplate();

    public ReturnPerso<KieServerInfo> verifyIfKieServerExist(String url) {
        String completeurl = url + "/api/server/details";
        logger.info("url pverifyIfKieServerExist :{} " , completeurl);

        HttpHeaders httpHeaders=new HttpHeaders();

        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<KieServerInfo> response = restTemplateKiewb.exchange(completeurl,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<>() {
                    });

            if (response.getBody() != null ) {
                return new ReturnPerso<>(true,"the runtime has been successfully added",response.getBody());
            } else {
                return new ReturnPerso<>(false,"server error ",null);
            }
        }catch (Exception e){
            return new ReturnPerso<>(false,"The hostname is incorrect",null);
        }
    }

    public void saveRuntime(RuntimePersist runtimePersist) {
        runtimeRepository.save(runtimePersist);
    }

    public RuntimeRepository getRuntimeRepository() {
        return runtimeRepository;
    }

    public void setRuntimeRepository(RuntimeRepository runtimeRepository) {
        this.runtimeRepository = runtimeRepository;
    }

    public void updateRuntimes(ProjectPersist projectPersist) {
        if (projectPersist!= null && projectPersist.getUuid()!= null) {
            List<ContainerPojoPersist> containerPojoPersists = containerRepository.findByProjectUUID(projectPersist.getUuid());
            for (ContainerPojoPersist containerPojoPersist : containerPojoPersists){
                containerPojoPersist.setDisableRuleLogging(projectPersist.isDisableRuleLogging());
                containerRepository.save(containerPojoPersist);

                List<ContainerRuntimePojoPersist> containerRuntimePojoPersists = containerRuntimeRepository.findByContainerId(containerPojoPersist.getContainerId());
                for (ContainerRuntimePojoPersist containerRuntimePojoPersist : containerRuntimePojoPersists){
                    containerRuntimePojoPersist.setDisableRuleLogging(projectPersist.isDisableRuleLogging());
                    containerRuntimePojoPersist.setProjectUUID(projectPersist.getUuid());
                    containerRuntimeRepository.save(containerRuntimePojoPersist);
                }
            }

        }
    }
}
