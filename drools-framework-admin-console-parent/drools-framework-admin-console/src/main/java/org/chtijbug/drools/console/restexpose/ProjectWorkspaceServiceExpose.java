package org.chtijbug.drools.console.restexpose;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.apache.commons.codec.binary.Base64;
import org.chtijbug.drools.console.service.IndexerService;
import org.chtijbug.drools.console.service.JobService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.proxy.persistence.model.KieWorkbench;
import org.chtijbug.drools.proxy.persistence.model.User;
import org.chtijbug.drools.proxy.persistence.model.UserGroups;
import org.chtijbug.drools.proxy.persistence.repository.KieWorkbenchRepository;
import org.chtijbug.drools.proxy.persistence.repository.UserGroupsRepository;
import org.chtijbug.drools.proxy.persistence.repository.UserRepository;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.guvnor.rest.client.CreateProjectJobRequest;
import org.guvnor.rest.client.CreateProjectRequest;
import org.guvnor.rest.client.Space;
import org.guvnor.rest.client.SpaceRequest;
import org.kie.server.api.model.KieServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@RestController
@RequestMapping("/api/wb")
@Api("/api/wb")
public class ProjectWorkspaceServiceExpose {

    @Autowired
    private IndexerService indexerService;

    private RestTemplate restTemplateKiewb = new RestTemplate();

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private KieWorkbenchRepository kieWorkbenchRepository;

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectPersistService projectPersistService;

    @PostMapping(value = "/spaces",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    @ApiOperation(value = "Create Workspace", notes = "Creating a workspace in specified workbench name in body", response = SpaceRequest.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = SpaceRequest.class)
    })
    public Response createWorkSpace(@RequestBody PlatformProjectData request) {
        String wbName = "demo";
        if (request.getWbName() != null
                && !request.getWbName().isEmpty()) {
            wbName = request.getWbName();
        }
        KieWorkbench kieWorkbench = kieWorkbenchRepository.findByName(wbName);
        if (kieWorkbench == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Workbench not found").build();
        }
        String completeurl = kieWorkbench.getExternalUrl() + "/rest/spaces";
        Space space = new Space();
        space.setName(request.getSpaceName());
        space.setDescription(request.getDescription());
        space.setDefaultGroupId(request.getGroupId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        space.setOwner(authentication.getName());

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        ResponseEntity<SpaceRequest> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.POST, requestCallback(space, connectedUser.getLogin(), connectedUser.getPassword()), clientHttpResponse -> {
                    SpaceRequest extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        extractedResponse = mapper.readValue(result, SpaceRequest.class);

                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        projectPersistService.createWorkSpaceGroupIfNeeded(request.getSpaceName(),kieWorkbench);
        jobService.executeWrite(kieWorkbench.getExternalUrl()+"/rest", connectedUser.getLogin(), connectedUser.getPassword(), null, response.getBody().getJobId(), null);
        Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
        return Response.status(Response.Status.CREATED).entity(response.getBody()).variant(variant).build();
    }

    @PostMapping(value = "/spaces/{spaceName}/projects",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    public Response createProject(@PathVariable("spaceName") String spaceName,@RequestBody PlatformProjectData request) {
        String wbName = "demo";
        if (request.getWbName() != null
                && !request.getWbName().isEmpty()) {
            wbName = request.getWbName();
        }
        KieWorkbench kieWorkbench = kieWorkbenchRepository.findByName(wbName);
        if (kieWorkbench == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Workbench  not found").build();
        }
        String completeurl = kieWorkbench.getExternalUrl() + "/rest/spaces/"+spaceName+"/projects";
        CreateProjectRequest projectRequest = new CreateProjectRequest();

        projectRequest.setName(request.getName());
        projectRequest.setGroupId(request.getGroupId());
        projectRequest.setVersion(request.getVersion());
        projectRequest.setDescription(request.getDescription());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        ResponseEntity<CreateProjectJobRequest> response = restTemplateKiewb
                .execute(completeurl, HttpMethod.POST, requestCallback(projectRequest, connectedUser.getLogin(), connectedUser.getPassword()), clientHttpResponse -> {
                    CreateProjectJobRequest extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        extractedResponse = mapper.readValue(result, CreateProjectJobRequest.class);

                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });
        jobService.executeWrite(kieWorkbench.getExternalUrl()+"/rest", connectedUser.getLogin(), connectedUser.getPassword(), null, response.getBody().getJobId(), null);

        completeurl = kieWorkbench.getExternalUrl() + "/rest/chtijbug/"+spaceName+"/"+projectRequest.getName()+"/dependency";
        ResponseEntity<PlatformProjectData> response2 = restTemplateKiewb
                .execute(completeurl, HttpMethod.PUT, requestCallback(request, connectedUser.getLogin(), connectedUser.getPassword()), clientHttpResponse -> {
                    PlatformProjectData extractedResponse = null;
                    if (clientHttpResponse.getBody() != null) {
                        Scanner s = new Scanner(clientHttpResponse.getBody()).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";
                        extractedResponse = mapper.readValue(result, PlatformProjectData.class);

                    }
                    return new ResponseEntity<>(extractedResponse, clientHttpResponse.getHeaders(), clientHttpResponse.getStatusCode());
                });


        Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
        UserGroups workspaceUsergroup = userGroupsRepository.findByName("wrk_" + spaceName);
        projectPersistService.createProjectGroupIfNeeded(request.getName(),kieWorkbench,null,workspaceUsergroup);
        return Response.status(Response.Status.CREATED).entity(response2.getBody()).variant(variant).build();
    }
    private RequestCallback requestCallback(final Object content, String username, String password) {
        return clientHttpRequest -> {

            if (content != null) {
                if (content instanceof KieServerInfo) {
                    KieServerInfo kieServerSetup = (KieServerInfo) content;

                    mapper.writeValue(clientHttpRequest.getBody(), kieServerSetup);
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

}
