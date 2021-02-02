package org.chtijbug.drools.console.middle;

import org.chtijbug.drools.console.dto.VariableData;
import org.chtijbug.drools.console.dto.VariableDefinition;
import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.proxy.persistence.model.*;
import org.chtijbug.drools.proxy.persistence.repository.*;
import org.chtijbug.guvnor.server.jaxrs.jaxb.Asset;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.drools.workbench.models.datamodel.rule.InterpolationVariable;
import org.drools.workbench.models.guided.template.backend.RuleTemplateModelXMLPersistenceImpl;
import org.drools.workbench.models.guided.template.shared.TemplateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DababaseContentUpdate {



    @Value("${kie-wb.mainwbintern}")
    private String mainwbUrlIntern;

    @Value("${kie-wb.mainwbextern}")
    private String mainwbExtern;

    @Value("${kie-wb.baseurl}")
    private String kiewbUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private KieWorkbenchRepository kieWorkbenchRepository;

    @Autowired
    private GuidedRulestemplateDefinitionRepository guidedRulestemplateDefinitionRepository;

    @Autowired
    private GuidedRulestemplateDataRepository guidedRulestemplateDataRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KieRepositoryService kieRepositoryService;

    @Autowired
    private ProjectPersistService projectPersistService;

    public void initDatabaseIfNecessary() {
/**
 *   admin       The administrator
 *   analyst     The analyst
 *   developer   The developer
 *   manager     The manager
 *   user        The end user
 *   kiemgmt     KIE management user
 */
        User adminUser = userRepository.findByLogin("admin");
        if (adminUser == null) {
            this.initDatabase();
            this.synchronizeDatabaseWithWorkbenches();
        } else {
            this.synchronizeDatabaseWithWorkbenches();
        }

    }

    private void initDatabase() {
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "process-admin"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "manager"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "admin"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "analyst"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "rest-all"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "developer"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "rest-project"));
        userRolesRepository.save(new UserRoles(UUID.randomUUID().toString(), "user"));


        userGroupsRepository.save(new UserGroups(UUID.randomUUID().toString(), "kiemgmt"));
        userGroupsRepository.save(new UserGroups(UUID.randomUUID().toString(), "admingroup"));
        userGroupsRepository.save(new UserGroups(UUID.randomUUID().toString(), "demogroup"));

        User adminUser = new User(UUID.randomUUID().toString(), "admin", "adminadmin99#");
       // adminUser.getUserGroups().add(userGroupsRepository.findByName("kiemgmt"));
       // adminUser.getUserGroups().add(userGroupsRepository.findByName("admingroup"));
        adminUser.getUserRoles().add(userRolesRepository.findByName("admin"));
        adminUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        userRepository.save(adminUser);

        User nheronUser = new User(UUID.randomUUID().toString(), "nheron", "adminnheron00@");
     //   nheronUser.getUserGroups().add(userGroupsRepository.findByName("kiemgmt"));
     //   nheronUser.getUserGroups().add(userGroupsRepository.findByName("admingroup"));
        nheronUser.getUserRoles().add(userRolesRepository.findByName("admin"));
        nheronUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        userRepository.save(nheronUser);

        User apiUser = new User(UUID.randomUUID().toString(), "api-user", "api-user");
        //   nheronUser.getUserGroups().add(userGroupsRepository.findByName("kiemgmt"));
        //   nheronUser.getUserGroups().add(userGroupsRepository.findByName("admingroup"));

        apiUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        userRepository.save(apiUser);

        KieWorkbench mainWorkbench = new KieWorkbench();
        mainWorkbench.setID(UUID.randomUUID().toString());
        mainWorkbench.setName("demo");
        mainWorkbench.setExternalUrl(mainwbExtern);
        mainWorkbench.setInternalUrl(mainwbUrlIntern);
        mainWorkbench = kieWorkbenchRepository.save(mainWorkbench);

        UserConnected userConnected = kieRepositoryService.getUserContent(mainWorkbench.getExternalUrl() + "/rest",
                nheronUser.getLogin(), nheronUser.getPassword(), mainWorkbench.getName());
        for (PlatformProjectData platformProjectData : userConnected.getProjectResponses()) {
            String projectName = platformProjectData.getName();
            String workspaceName = platformProjectData.getSpaceName();
            ProjectPersist projectPersist = projectPersistService.saveorUpdateProject(platformProjectData, mainWorkbench);
            UserGroups workspaceUserGroups = projectPersistService.createWorkSpaceGroupIfNeeded(workspaceName, mainWorkbench);
            String result=kieRepositoryService.createSpaceRight(mainWorkbench.getExternalUrl() + "/rest",
                    nheronUser.getLogin(), nheronUser.getPassword(), mainWorkbench.getName(),workspaceUserGroups.getName(),workspaceName);
            projectPersistService.createProjectGroupIfNeeded(projectName, mainWorkbench, projectPersist, workspaceUserGroups);

            //platformProjectData.getJavaClasses()


        }
        Customer demCustomer = new Customer();
        demCustomer.setKieWorkbench(mainWorkbench);
        demCustomer.setName("demoCustomer");
        demCustomer.setID(UUID.randomUUID().toString());
        customerRepository.save(demCustomer);
        User demoUser = new User(UUID.randomUUID().toString(), "demo", "demo");
        demoUser.getUserGroups().add(userGroupsRepository.findByName("demogroup"));
        demoUser.getUserRoles().add(userRolesRepository.findByName("user"));
        demoUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        demoUser.getUserRoles().add(userRolesRepository.findByName("analyst"));
        demoUser.setWbName(mainWorkbench.getName());
        demoUser.setCustomer(demCustomer);
        userRepository.save(demoUser);
    }

    public void updateOrCreateLineGuidedRuleTemplate(UserGroups spaceGroup,UserGroups projectGroup,String assetName, GuidedRulesTemplateData guidedRulesTemplateData){

    }

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void synchronizeDatabaseWithWorkbenches() {
        guidedRulestemplateDefinitionRepository.deleteAll();
        guidedRulestemplateDataRepository.deleteAll();
        for (KieWorkbench kieWorkbench : kieWorkbenchRepository.findAll()) {
            User nheronUser = userRepository.findByLogin("nheron");
            UserConnected userConnected = kieRepositoryService.getUserContent(kieWorkbench.getExternalUrl() + "/rest",
                    nheronUser.getLogin(), nheronUser.getPassword(), kieWorkbench.getName());
            for (PlatformProjectData platformProjectData : userConnected.getProjectResponses()) {

                String projectName = platformProjectData.getName();
                String workspaceName = platformProjectData.getSpaceName();
                ProjectPersist projectPersist = projectPersistService.saveorUpdateProject(platformProjectData, kieWorkbench);
                UserGroups workSpaceGroupIfNeeded = projectPersistService.createWorkSpaceGroupIfNeeded(workspaceName, kieWorkbench);
                UserGroups projectGroupIfNeeded = projectPersistService.createProjectGroupIfNeeded(projectName, kieWorkbench, projectPersist, workSpaceGroupIfNeeded);

                List<Asset> assets = kieRepositoryService.getListAssets(kieWorkbench.getExternalUrl() + "/rest", nheronUser.getLogin(), nheronUser.getPassword(), workspaceName, projectName);
                for (Asset asset : assets) {
                    if (asset.getTitle().contains(".template")) {

                        GuidedRulesTemplateDefinition guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.findByTemplateNameAndProjectGroup(asset.getTitle(), projectGroupIfNeeded);
                        if (guidedRulesTemplateDefinition == null) {
                            guidedRulesTemplateDefinition = new GuidedRulesTemplateDefinition();
                            guidedRulesTemplateDefinition.setTemplateName(asset.getTitle());

                            guidedRulesTemplateDefinition.setProjectGroup(workSpaceGroupIfNeeded);

                        }
                        String assetSource = kieRepositoryService.getAssetSource(kieWorkbench.getExternalUrl() + "/rest", nheronUser.getLogin(), nheronUser.getPassword(), workspaceName, projectName, asset.getTitle());
                        TemplateModel model = RuleTemplateModelXMLPersistenceImpl.getInstance().unmarshal(assetSource);
                        guidedRulesTemplateDefinition.getVariables().clear();
                        for (InterpolationVariable interpolationVariable : model.getInterpolationVariablesList()) {
                            VariableDefinition variableDefinition = new VariableDefinition();
                            variableDefinition.setVarName(interpolationVariable.getVarName());
                            variableDefinition.setDataType(interpolationVariable.getDataType());
                            variableDefinition.setFactField(interpolationVariable.getFactField());
                            variableDefinition.setFactType(interpolationVariable.getFactType());
                            variableDefinition.setOperator(interpolationVariable.getOperator());
                            guidedRulesTemplateDefinition.getVariables().add(variableDefinition);
                        }
                        guidedRulesTemplateDefinition = guidedRulestemplateDefinitionRepository.save(guidedRulesTemplateDefinition);
                        Map<String, VariableDefinition> variableDefinitionMap = new HashMap<>();
                        for (VariableDefinition variableDefinition : guidedRulesTemplateDefinition.getVariables()) {
                            variableDefinitionMap.put(variableDefinition.getVarName(), variableDefinition);
                        }
                        Map<String, List<String>> dataTable = model.getTable();
                        int nbRows = model.getRowsCount();
                        for (int i = 0; i < nbRows; i++) {
                            GuidedRulesTemplateData guidedRulesTemplateData=guidedRulestemplateDataRepository.findByGuidedRulesTemplateDefinitionAndLineID(guidedRulesTemplateDefinition,String.valueOf(i));
                            String colid = dataTable.get(TemplateModel.ID_COLUMN_NAME).get(i);
                            if (guidedRulesTemplateData==null) {
                                guidedRulesTemplateData = new GuidedRulesTemplateData();
                                guidedRulesTemplateData.setGuidedRulesTemplateDefinition(guidedRulesTemplateDefinition);
                                guidedRulesTemplateData.setLineID(colid);
                            }

                            for (VariableDefinition variableDefinition : guidedRulesTemplateDefinition.getVariables()) {
                                String data = dataTable.get(variableDefinition.getVarName()).get(i);
                                VariableData variableData= new VariableData();
                                variableData.setVarName(variableDefinition.getVarName());
                                if ("String".equals(variableDefinition.getDataType())){
                                    variableData.setStringValue(data);
                                }else if ("Long".equals(variableDefinition.getDataType())){
                                    if (data!=null && data.length()>0) {
                                        variableData.setLongValue(Long.parseLong(data));
                                    }
                                }else if ("Double".equals(variableDefinition.getDataType())){
                                    if (data!=null && data.length()>0) {
                                        variableData.setDoubleValue(Double.parseDouble(data));
                                    }
                                }else if ("BigDecimal".equals(variableDefinition.getDataType())){
                                    if (data!=null && data.length()>0) {
                                        variableData.setBigDecimalValue(BigDecimal.valueOf(Double.parseDouble(data)));
                                    }
                                }
                                guidedRulesTemplateData.getRows().add(variableData);
                            }
                            guidedRulestemplateDataRepository.save(guidedRulesTemplateData);
                        }

                    }
                }

            }
        }

    }

}
