package org.chtijbug.drools.console.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import org.apache.commons.codec.binary.Base64;
import org.chtijbug.drools.console.AddLog;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.console.service.model.kie.JobStatus;
import org.chtijbug.drools.proxy.persistence.model.User;
import org.chtijbug.drools.proxy.persistence.repository.UserRepository;
import org.chtijbug.guvnor.server.jaxrs.api.UserLoginInformation;
import org.chtijbug.guvnor.server.jaxrs.jaxb.Asset;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.chtijbug.guvnor.server.jaxrs.model.WorkspaceAuthData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Service
@DependsOn("applicationContext")
public class KieRepositoryService {


    private static Logger logger = LoggerFactory.getLogger(KieRepositoryService.class);

    private static String chtijbugprefix="/chtijbug/";

    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplateKiewb = new RestTemplate();

    private ObjectMapper mapper = new ObjectMapper();


    public void updateAssetSource(String url, String username, String password, String spaceName, String projectName, String assetName, String assetSource) {

        String completeurl = url + chtijbugprefix + spaceName + "/" + projectName + "/asset/" + assetName + "/source";
        logger.info("url updateAssetSource : {}",  completeurl);

       restTemplateKiewb
                .execute(completeurl, HttpMethod.PUT, requestCallback(assetSource, username, password), clientHttpResponse -> {
                    String extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        extractedResponse = result;
                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
    }

    public String getAssetSource(String url, String username, String password, String spaceName, String projectName, String assetName) {
        String completeurl = url + chtijbugprefix + spaceName + "/" + projectName + "/assets/" + assetName + "/source";
        logger.info("url getAssetSource : {}", completeurl);
        ResponseEntity<String> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                    String result=null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        result = s.hasNext() ? s.next() : "";
                    }
                    return new ResponseEntity<>(result, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        String reponseMoteur;

        reponseMoteur = response.getBody();
        return reponseMoteur;
    }

    public List<PlatformProjectData> getListSpaces2(String url, String username, String password) {
        String completeurl = url + chtijbugprefix+"detailedSpaces";
        logger.info("url getListSpaces2 :{} ", completeurl);
        ResponseEntity<List<PlatformProjectData>> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                    List<PlatformProjectData> extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        PlatformProjectData[] values = mapper.readValue(result, PlatformProjectData[].class);
                        extractedResponse = Arrays.asList(values);
                    }
                     return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        List<PlatformProjectData> reponseMoteur;

        reponseMoteur = response.getBody();
        return reponseMoteur;
    }

    public UserConnected login(String url, String username, String password,String workbenchName) {

        User user = userRepository.findByLogin(username);
        String completeurl = url + chtijbugprefix+"login";
        if (user != null && user.getPassword().equals(password)) {
            if (user.getCustomer()!= null &&
                    user.getCustomer().getKieWorkbench()!= null
                    && user.getCustomer().getKieWorkbench().getInternalUrl()!= null){
                completeurl = user.getCustomer().getKieWorkbench().getInternalUrl()+"/rest/chtijbug/login";
            }

            logger.info("url moteur reco : {}" , completeurl);
            ResponseEntity<UserLoginInformation> response = restTemplateKiewb
                    .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                        UserLoginInformation extractedResponse = null;
                        if (clientHttpResponse.getBody() != null) {
                            Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";
                            extractedResponse = mapper.readValue(result, UserLoginInformation.class);

                        }
                         return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                    });
            UserConnected userConnected = new UserConnected();


            userConnected.setUserName(username);
            userConnected.setUserPassword(password);
            userConnected.setUserPassword(password);
            if (response!= null) {
                UserLoginInformation responseBody = response.getBody();
                if (responseBody!= null) {
                    userConnected.getProjectResponses().addAll(responseBody.getProjects());
                    userConnected.getRoles().addAll(responseBody.getRoles());
                }
            }
            userConnected.setUserName(username);
            userConnected.setKieWorkbenchName(workbenchName);
            return userConnected;
        } else {
            return null;
        }
    }
    public UserConnected getUserContent(String url, String username, String password,String workbenchName) {

        User user = userRepository.findByLogin(username);
        String completeurl = url + chtijbugprefix+"content";
        if (user != null && user.getPassword().equals(password)) {
            if (user.getCustomer()!= null &&
                    user.getCustomer().getKieWorkbench()!= null
                    && user.getCustomer().getKieWorkbench().getInternalUrl()!= null){
                completeurl = user.getCustomer().getKieWorkbench().getInternalUrl()+"/rest/chtijbug/content";
            }

            logger.info("url moteur reco : {}" , completeurl);
            ResponseEntity<UserLoginInformation> response = restTemplateKiewb
                    .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                        UserLoginInformation extractedResponse =null;
                        if (clientHttpResponse.getBody() != null) {
                            Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";
                            extractedResponse = mapper.readValue(result, UserLoginInformation.class);

                        }
                        return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                    });
            UserConnected userConnected = new UserConnected();

            UserLoginInformation responseBody = response.getBody();
            userConnected.setUserName(username);
            userConnected.setUserPassword(password);
            userConnected.setUserPassword(password);
            userConnected.getProjectResponses().addAll(responseBody.getProjects());
            userConnected.getRoles().addAll(responseBody.getRoles());
            userConnected.setUserName(username);
            userConnected.setKieWorkbenchName(workbenchName);
            return userConnected;
        } else {
            return null;
        }
    }
    public List<Asset> getListAssets(String url, String username, String password, String spaceName, String projectName) {
        String completeurl = url + chtijbugprefix + spaceName + "/" + projectName + "/assets";
        logger.info("url getListAssets : {}" , completeurl);
        ResponseEntity<List<Asset>> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                    List<Asset> extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        Asset[] values = mapper.readValue(result, Asset[].class);
                        extractedResponse = Arrays.asList(values);
                    }
                     return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        List<Asset> reponseMoteur;

        reponseMoteur = response.getBody();
        return reponseMoteur;
    }

    public JobStatus buildProject(String url, String username, String password, String space, String project, String branchName,String command, AddLog workOnGoingView, UI ui) {
        String completeurl= url + "/spaces/" + space + "/projects/" + project + "/maven/" + command;
        if (branchName!= null && branchName.length()>0){
            completeurl= url + "/spaces/" + space + "/projects/" + project +"/branches/"+branchName+ "/maven/" + command;
        }
        logger.info("url buildProject Maven install : {}" , completeurl);
        ResponseEntity<JobStatus> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.POST, requestCallback(null, username, password), clientHttpResponse -> {
                    JobStatus extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        JobStatus values = mapper.readValue(result, JobStatus.class);
                        extractedResponse = values;
                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        JobStatus reponseMoteur;

        reponseMoteur = response.getBody();
        workOnGoingView.addRow(reponseMoteur.toString(), ui);
        return reponseMoteur;
    }

    public JobStatus getStatusJobID(String url, String username, String password, String jobID) {

        String completeurl = url + "/jobs/" + jobID;
        logger.info("url getStatusJobID : {}" , completeurl);
        ResponseEntity<JobStatus> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.GET, requestCallback(null, username, password), clientHttpResponse -> {
                    JobStatus extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        extractedResponse = mapper.readValue(result, JobStatus.class);

                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        JobStatus reponseMoteur;

        reponseMoteur = response.getBody();
        return reponseMoteur;
    }



    private RequestCallback requestCallback(final Object content, String username, String password) {
        return clientHttpRequest -> {
            if (content != null) {
                if (content instanceof String) {
                    String stringContent = (String) content;
                    stringContent = stringContent.replace("\"", "");
                    mapper.writeValue(clientHttpRequest.getBody(), stringContent);
                } else {
                    mapper.writeValue(clientHttpRequest.getBody(), content);
                }
            }
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            clientHttpRequest.getHeaders().add(
                    HttpHeaders.AUTHORIZATION, authHeader);
        };
    }
    public String createSpaceRight(String url, String username, String password,String workbenchName,String groupName,String spaceName) {

        User user = userRepository.findByLogin(username);
        String completeurl = url + chtijbugprefix+"auth";
        if (user != null && user.getPassword().equals(password)) {
            if (user.getCustomer()!= null &&
                    user.getCustomer().getKieWorkbench()!= null
                    && user.getCustomer().getKieWorkbench().getInternalUrl()!= null){
                completeurl = user.getCustomer().getKieWorkbench().getInternalUrl()+"/rest/chtijbug/auth";
            }
            completeurl=completeurl+"/"+groupName+"/"+spaceName;
            logger.info("url moteur reco : {}" , completeurl);
            ResponseEntity<WorkspaceAuthData> response = restTemplateKiewb
                    .execute(completeurl, HttpMethod.POST, requestCallback(null, username, password), clientHttpResponse -> {
                        WorkspaceAuthData extractedResponse =null;
                        if (clientHttpResponse.getBody() != null) {
                            Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";
                            extractedResponse = mapper.readValue(result, WorkspaceAuthData.class);

                        }
                        return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                    });


            WorkspaceAuthData responseBody = response.getBody();

            return responseBody.getStatus();
        } else {
            return null;
        }
    }


}
