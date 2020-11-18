package org.chtijbug.drools.console.restexpose;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.apache.commons.codec.binary.Base64;
import org.chtijbug.drools.console.dto.GuidedRuleTemplate;
import org.chtijbug.drools.console.dto.GuidedRuleTemplateDataRow;
import org.chtijbug.drools.console.dto.VariableData;
import org.chtijbug.drools.console.dto.VariableDefinition;
import org.chtijbug.drools.console.middle.DababaseContentUpdate;
import org.chtijbug.drools.console.service.IndexerService;
import org.chtijbug.drools.console.service.JobService;
import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.proxy.persistence.model.*;
import org.chtijbug.drools.proxy.persistence.repository.*;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.drools.workbench.models.datamodel.rule.InterpolationVariable;
import org.drools.workbench.models.datamodel.rule.util.InterpolationVariableCollector;
import org.drools.workbench.models.datamodel.rule.visitors.RuleModelVisitor;
import org.drools.workbench.models.guided.template.backend.RuleTemplateModelXMLPersistenceImpl;
import org.drools.workbench.models.guided.template.shared.TemplateModel;
import org.guvnor.rest.client.SpaceRequest;
import org.kie.server.api.model.KieServerInfo;
import org.kie.soup.project.datamodel.oracle.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.drools.workbench.models.guided.template.shared.TemplateModel.ID_COLUMN_NAME;

@RestController
@RequestMapping("/api/asset")
@Api("/api/asset")
public class AssetServiceExpose {

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
    private GuidedRulestemplateDefinitionRepository guidedRulestemplateDefinitionRepository;

    @Autowired
    private GuidedRulestemplateDataRepository guidedRulestemplateDataRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectPersistService projectPersistService;

    @Autowired
    private KieRepositoryService kieRepositoryService;

    @GetMapping(value = "/{spaceName}/{projectName}/grt/{templateName}/data",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get Template dta", notes = "Sends back data of a guided rule template", response = GuidedRuleTemplate.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = GuidedRuleTemplate.class)
    })
    public Response getTemplateData(@PathVariable("spaceName") String spaceName, @PathVariable("projectName") String projectName, @PathVariable("templateName") String templateName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        UserGroups userGroupsSpace = userGroupsRepository.findBySpaceName(spaceName);
        if (userGroupsSpace == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Space not found").build();
        }


        UserGroups userGroupsProject = userGroupsRepository.findByWorkspaceUserGroupAndProjectName(userGroupsSpace, projectName);
        if (userGroupsProject == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Project not found").build();
        }
        if (!connectedUser.getUserRoles().contains(userRolesRepository.findByName("admin"))
                && !connectedUser.getUserGroups().contains(userGroupsSpace)
                && !connectedUser.getUserGroups().contains(userGroupsProject)) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not allowed to access workspace/project").build();
        }
        GuidedRulesTemplateDefinition guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.findByTemplateNameAndProjectGroup(templateName, userGroupsProject);
        if (guidedRulesTemplateDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Guided rule template not found").build();
        }
        GuidedRuleTemplate guidedRuleTemplate = new GuidedRuleTemplate();
        for (VariableDefinition elt : guidedRulesTemplateDefinition.getVariables()) {
            guidedRuleTemplate.getDefinitionList().put(elt.getVarName(), elt.getDataType());
        }

        guidedRuleTemplate.setName(templateName);
        guidedRuleTemplate.setSpaceName(spaceName);
        guidedRuleTemplate.setProjectName(projectName);
        if (userGroupsSpace.getKieWorkbench() != null) {
            guidedRuleTemplate.setWorkbenchName(userGroupsSpace.getKieWorkbench().getName());
        }
        List<GuidedRulesTemplateData> guidedRulesTemplateDatas = guidedRulestemplateDataRepository.findByGuidedRulesTemplateDefinition(guidedRulesTemplateDefinition);

        for (GuidedRulesTemplateData elt : guidedRulesTemplateDatas) {
            GuidedRuleTemplateDataRow guidedRuleTemplateDataRow = new GuidedRuleTemplateDataRow();
            guidedRuleTemplate.getRows().add(guidedRuleTemplateDataRow);
            guidedRuleTemplateDataRow.setLineID(elt.getLineID());
            for (VariableData elt2 : elt.getRows()) {
                if (elt2.getVarName()!= null) {
                    if (elt2.getStringValue() != null) {
                        guidedRuleTemplateDataRow.getDataList().put(elt2.getVarName(), elt2.getStringValue());
                    } else if (elt2.getBigDecimalValue() != null) {
                        guidedRuleTemplateDataRow.getDataList().put(elt2.getVarName(), elt2.getBigDecimalValue());
                    } else if (elt2.getDoubleValue() != null) {
                        guidedRuleTemplateDataRow.getDataList().put(elt2.getVarName(), elt2.getDoubleValue());
                    } else if (elt2.getLongValue() != null) {
                        guidedRuleTemplateDataRow.getDataList().put(elt2.getVarName(), elt2.getLongValue());
                    }
                }
            }

        }
        Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
        return Response.status(Response.Status.CREATED).entity(guidedRuleTemplate).variant(variant).build();
    }

    @PutMapping(value = "/{spaceName}/{projectName}/grt/{templateName}/data/{lineID}",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    @ApiOperation(value = "update Template dta row", notes = "updates row of a guided rule template", response = GuidedRuleTemplateDataRow.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = GuidedRuleTemplateDataRow.class)
    })
    public Response updateTemplateData(@PathVariable("spaceName") String spaceName, @PathVariable("projectName") String projectName,
                                       @PathVariable("templateName") String templateName, @PathVariable("lineID") String lineID,
                                       @RequestBody GuidedRuleTemplateDataRow updatedRow) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        UserGroups userGroupsSpace = userGroupsRepository.findBySpaceName(spaceName);
        if (userGroupsSpace == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Space not found").build();
        }


        UserGroups userGroupsProject = userGroupsRepository.findByWorkspaceUserGroupAndProjectName(userGroupsSpace, projectName);
        if (userGroupsProject == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Project not found").build();
        }
        if (!connectedUser.getUserRoles().contains(userRolesRepository.findByName("admin"))
                && !connectedUser.getUserGroups().contains(userGroupsSpace)
                && !connectedUser.getUserGroups().contains(userGroupsProject)) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not allowed to access workspace/project").build();
        }
        GuidedRulesTemplateDefinition guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.findByTemplateNameAndProjectGroup(templateName, userGroupsProject);
        if (guidedRulesTemplateDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Guided rule template not found").build();
        }
        if (updatedRow == null) {
            return Response.status(Response.Status.NO_CONTENT).entity("No data ").build();
        }

        GuidedRulesTemplateData guidedRulesTemplateData = guidedRulestemplateDataRepository.findByGuidedRulesTemplateDefinitionAndLineID(guidedRulesTemplateDefinition, lineID);
        if (guidedRulesTemplateData == null) {
            return Response.status(Response.Status.NO_CONTENT).entity("Line ID not existing ").build();
        }
        Map<String, VariableDefinition> defData = new HashMap<>();
        for (VariableDefinition dataDefinition : guidedRulesTemplateDefinition.getVariables()) {
            defData.put(dataDefinition.getVarName(), dataDefinition);
        }
        String assetSource = kieRepositoryService.getAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName);
        TemplateModel model = RuleTemplateModelXMLPersistenceImpl.getInstance().unmarshal(assetSource);
        Object[] indexes = model.getTable().get(ID_COLUMN_NAME).toArray();
        int foundIndex=0;
        boolean found=false;
        for (Object o : indexes){
            if (o.equals(lineID)){
                found=true;
                break;
            }
            foundIndex++;
        }
        if (found) {
            Map<InterpolationVariable, Integer> variableIntegerMap = this.getInterpolationVariables(model);
            String[] rowContent = new String[variableIntegerMap.size() - 1];

            for (Map.Entry<InterpolationVariable, Integer> entry : variableIntegerMap.entrySet()) {
                if (!entry.getKey().getVarName().equals(ID_COLUMN_NAME)) {

                    Object data = updatedRow.getDataList().get(entry.getKey().getVarName());
                    if (data != null) {
                        model.setValue(entry.getKey().getVarName(), foundIndex, data.toString());
                    } else {
                        model.setValue(entry.getKey().getVarName(), foundIndex, "");
                    }
                }
            }

            String newAssetSource=RuleTemplateModelXMLPersistenceImpl.getInstance().marshal(model);
            kieRepositoryService.updateAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName,newAssetSource);

            Enumeration e = updatedRow.getDataList().keys();
            while (e.hasMoreElements()) {
                String variableName = (String) e.nextElement();
                Object variableData = updatedRow.getDataList().get(variableName);
                for (VariableData variableData1 : guidedRulesTemplateData.getRows()) {
                    VariableDefinition dataDefinition = defData.get(variableData1.getVarName());
                    if (dataDefinition.getVarName().equals(variableName)) {
                        if ("String".equals(dataDefinition.getDataType())) {
                            variableData1.setStringValue((String) variableData);
                        } else if ("Long".equals(dataDefinition.getDataType())) {
                            if (variableData != null) {
                                Long ll = (Long) variableData;
                                variableData1.setLongValue(ll);
                            }
                        } else if ("Double".equals(dataDefinition.getDataType())) {
                            if (variableData != null) {
                                Double dd = (Double) variableData;
                                variableData1.setDoubleValue(dd);
                            }
                        } else if ("BigDecimal".equals(dataDefinition.getDataType())) {
                            if (variableData != null) {
                                BigDecimal bg = (BigDecimal) variableData;
                                variableData1.setBigDecimalValue(bg);
                            }
                        }
                    }

                }

            }
            guidedRulestemplateDataRepository.save(guidedRulesTemplateData);
            Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
            return Response.status(Response.Status.OK).entity(updatedRow).variant(variant).build();
        }else{
            Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
            return Response.status(Response.Status.NOT_FOUND).entity(updatedRow).variant(variant).build();
        }
    }

    @PostMapping(value = "/{spaceName}/{projectName}/grt/{templateName}/data",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    @ApiOperation(value = "create Template dta row", notes = "creates row of a guided rule template", response = GuidedRuleTemplateDataRow.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = GuidedRuleTemplateDataRow.class)
    })


    public Response createTemplateData(@PathVariable("spaceName") String spaceName, @PathVariable("projectName") String projectName,
                                       @PathVariable("templateName") String templateName,
                                       @RequestBody GuidedRuleTemplateDataRow updatedRow) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        UserGroups userGroupsSpace = userGroupsRepository.findBySpaceName(spaceName);
        if (userGroupsSpace == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Space not found").build();
        }
        UserGroups userGroupsProject = userGroupsRepository.findByWorkspaceUserGroupAndProjectName(userGroupsSpace, projectName);
        if (userGroupsProject == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Project not found").build();
        }
        if (!connectedUser.getUserRoles().contains(userRolesRepository.findByName("admin"))
                && !connectedUser.getUserGroups().contains(userGroupsSpace)
                && !connectedUser.getUserGroups().contains(userGroupsProject)) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not allowed to access workspace/project").build();
        }
        GuidedRulesTemplateDefinition guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.findByTemplateNameAndProjectGroup(templateName, userGroupsProject);
        if (guidedRulesTemplateDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Guided rule template not found").build();
        }
        if (updatedRow == null) {
            return Response.status(Response.Status.NO_CONTENT).entity("No data ").build();
        }

        GuidedRulesTemplateData guidedRulesTemplateData = new GuidedRulesTemplateData();
        guidedRulesTemplateData.setGuidedRulesTemplateDefinition(guidedRulesTemplateDefinition);
        Map<String, VariableDefinition> defData = new HashMap<>();
        for (VariableDefinition dataDefinition : guidedRulesTemplateDefinition.getVariables()) {
            defData.put(dataDefinition.getVarName(), dataDefinition);
        }
        String assetSource = kieRepositoryService.getAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName);
        TemplateModel model = RuleTemplateModelXMLPersistenceImpl.getInstance().unmarshal(assetSource);
        Map<InterpolationVariable, Integer> variableIntegerMap = this.getInterpolationVariables(model);
        String[] rowContent = new String[variableIntegerMap.size()-1];

        for (Map.Entry<InterpolationVariable, Integer> entry : variableIntegerMap.entrySet()){
            if (!entry.getKey().getVarName().equals(ID_COLUMN_NAME)) {
                Object data = updatedRow.getDataList().get(entry.getKey().getVarName());
                if (data != null) {
                    rowContent[entry.getValue()] = data.toString();
                } else {
                    rowContent[entry.getValue()] = "";
                }
            }
        }
        String lineID = model.addRow(rowContent);
        guidedRulesTemplateData.setLineID(lineID);

        String newAssetSource=RuleTemplateModelXMLPersistenceImpl.getInstance().marshal(model);
       kieRepositoryService.updateAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName,newAssetSource);

        Enumeration e = updatedRow.getDataList().keys();
        while (e.hasMoreElements()) {
            String variableName = (String) e.nextElement();
            Object variableData = updatedRow.getDataList().get(variableName);
            VariableData variableData1 = new VariableData();
            guidedRulesTemplateData.getRows().add(variableData1);
            variableData1.setVarName(variableName);
            VariableDefinition dataDefinition = defData.get(variableName);
            if (dataDefinition!= null
                && dataDefinition.getVarName().equals(variableName)) {
                if ("String".equals(dataDefinition.getDataType())) {
                    variableData1.setStringValue((String) variableData);
                } else if ("Long".equals(dataDefinition.getDataType())) {
                    if (variableData != null) {
                        Long ll = (Long) variableData;
                        variableData1.setLongValue(ll);
                    }
                } else if ("Double".equals(dataDefinition.getDataType())) {
                    if (variableData != null) {
                        Double dd = (Double) variableData;
                        variableData1.setDoubleValue(dd);
                    }
                } else if ("BigDecimal".equals(dataDefinition.getDataType())) {
                    if (variableData != null) {
                        BigDecimal bg = (BigDecimal) variableData;
                        variableData1.setBigDecimalValue(bg);
                    }
                }
            }
        }
        guidedRulestemplateDataRepository.save(guidedRulesTemplateData);
        Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
        updatedRow.setLineID(lineID);
        return Response.status(Response.Status.OK).entity(updatedRow).variant(variant).build();
    }

    @DeleteMapping(value = "/{spaceName}/{projectName}/grt/{templateName}/data/{lineID}",
            consumes = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML},
            produces = {javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
    @ApiOperation(value = "delete Template dta row", notes = "deletes row of a guided rule template", response = GuidedRuleTemplateDataRow.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Response.class)
    })
    public Response deleteemplateData(@PathVariable("spaceName") String spaceName, @PathVariable("projectName") String projectName,
                                       @PathVariable("templateName") String templateName, @PathVariable("lineID") String lineID) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        User connectedUser = userRepository.findByLogin(currentPrincipalName);
        UserGroups userGroupsSpace = userGroupsRepository.findBySpaceName(spaceName);
        if (userGroupsSpace == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Space not found").build();
        }


        UserGroups userGroupsProject = userGroupsRepository.findByWorkspaceUserGroupAndProjectName(userGroupsSpace, projectName);
        if (userGroupsProject == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Project not found").build();
        }
        if (!connectedUser.getUserRoles().contains(userRolesRepository.findByName("admin"))
                && !connectedUser.getUserGroups().contains(userGroupsSpace)
                && !connectedUser.getUserGroups().contains(userGroupsProject)) {
            return Response.status(Response.Status.NOT_FOUND).entity("User not allowed to access workspace/project").build();
        }
        GuidedRulesTemplateDefinition guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.findByTemplateNameAndProjectGroup(templateName, userGroupsProject);
        if (guidedRulesTemplateDefinition == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Guided rule template not found").build();
        }


        GuidedRulesTemplateData guidedRulesTemplateData = guidedRulestemplateDataRepository.findByGuidedRulesTemplateDefinitionAndLineID(guidedRulesTemplateDefinition, lineID);
        if (guidedRulesTemplateData == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Line ID  not existing ").build();
        }
        String assetSource = kieRepositoryService.getAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName);
        TemplateModel model = RuleTemplateModelXMLPersistenceImpl.getInstance().unmarshal(assetSource);
        model.removeRowById(lineID);
        String newAssetSource=RuleTemplateModelXMLPersistenceImpl.getInstance().marshal(model);
        kieRepositoryService.updateAssetSource(userGroupsSpace.getKieWorkbench().getExternalUrl() + "/rest", connectedUser.getLogin(), connectedUser.getPassword(), spaceName, projectName, templateName,newAssetSource);

        guidedRulestemplateDataRepository.delete(guidedRulesTemplateData);
        Variant variant = Variant.mediaTypes(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
        return Response.status(Response.Status.OK).variant(variant).build();
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
    private Map<InterpolationVariable, Integer> getInterpolationVariables(TemplateModel templateModel) {
        final Map<InterpolationVariable, Integer> variables = new HashMap<InterpolationVariable, Integer>();

        new RuleModelVisitor(variables).visit(templateModel);

        final Map<InterpolationVariable, Integer> result = new InterpolationVariableCollector(variables).getMap();

        InterpolationVariable id = new InterpolationVariable(ID_COLUMN_NAME,
                DataType.TYPE_NUMERIC_LONG);
        result.put(id,
                result.size());
        return result;
    }

}
