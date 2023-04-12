package org.chtijbug.drools.console.service;

import com.vaadin.flow.component.UI;
import org.chtijbug.drools.ReverseProxyUpdate;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.chtijbug.drools.console.AddLog;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.console.service.model.kie.JobStatus;
import org.chtijbug.drools.console.service.model.kie.KieConfigurationData;
import org.chtijbug.drools.console.service.util.AppContext;
import org.chtijbug.drools.jms.ReverseProxyMessageCreator;
import org.chtijbug.drools.proxy.persistence.json.KieProject;
import org.chtijbug.drools.proxy.persistence.model.*;
import org.chtijbug.drools.proxy.persistence.repository.*;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@DependsOn("applicationContext")
public class ProjectPersistService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectPersistService.class);

    private static String projectVariable = "4";

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private KieRepositoryService kieRepositoryService;

    private KieConfigurationData config;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;


    @Autowired
    private UserConnectedService userConnectedService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private ContainerRuntimeRepository containerRuntimeRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private KieWorkbenchRepository workbenchRepository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private UserGroupsRepository userGroupsRepository;


    public ProjectPersistService() {
        this.config = AppContext.getApplicationContext().getBean(KieConfigurationData.class);

    }

    public ProjectPersist saveorUpdateProject(PlatformProjectData platformProjectData, KieWorkbench kieWorkbench) {
        ProjectPersist projectPersist = projectRepository.findByProjectNameAndBranch(new KieProject(platformProjectData.getSpaceName(), platformProjectData.getName()), platformProjectData.getBranch());

        if (projectPersist == null) {
            projectPersist = platformProjectResponseToProjectPersist(platformProjectData);
            projectPersist.setKieWorkbench(kieWorkbench);
            projectPersist.setProjectVersion(platformProjectData.getVersion());
            projectPersist.setArtifactID(platformProjectData.getArtifactId());
            projectPersist.setGroupID(platformProjectData.getGroupId());
            projectPersist.setClassNameList(new ArrayList<>());
            for (String className : platformProjectData.getJavaClasses()) {
                projectPersist.getClassNameList().add(className);

            }
            projectPersist = projectRepository.save(projectPersist);

        } else {
            projectPersist.setKieWorkbench(kieWorkbench);
            projectPersist.setProjectVersion(platformProjectData.getVersion());
            projectPersist.setArtifactID(platformProjectData.getArtifactId());
            projectPersist.setGroupID(platformProjectData.getGroupId());
            projectPersist.setClassNameList(new ArrayList<>());
            for (String className : platformProjectData.getJavaClasses()) {
                projectPersist.getClassNameList().add(className);

            }
            projectRepository.save(projectPersist);

        }
        return projectPersist;
    }

    public UserGroups createProjectGroupIfNeeded(String projectName, KieWorkbench kieWorkbench, ProjectPersist projectPersist, UserGroups workspaceUserGroup) {
        UserGroups userGroups = userGroupsRepository.findByName("prj_" + projectName);
        if (userGroups == null) {
            UserGroups projectGroup = new UserGroups( "prj_" + projectName);
            projectGroup.setKieWorkbench(kieWorkbench);
            projectGroup.setProjectName(projectName);
            projectGroup.setProjectPersist(projectPersist);
            projectGroup.setWorkspaceUserGroup(workspaceUserGroup);
            projectGroup = userGroupsRepository.save(projectGroup);
            User groupUser = new User( "prj_user_" + projectName, "adminadmin99#");
            groupUser.getUserGroups().add(projectGroup);
            groupUser.getUserRoles().add(userRolesRepository.findByName("analyst"));
            groupUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
            userRepository.save(groupUser);
        } else {
            userGroups.setWorkspaceUserGroup(workspaceUserGroup);
            userGroups=userGroupsRepository.save(userGroups);
        }
        return userGroups;
    }

    public UserGroups createWorkSpaceGroupIfNeeded(String workSpaceName, KieWorkbench kieWorkbench) {
        UserGroups userGroupsWorkSpace = userGroupsRepository.findByName("wrk_" + workSpaceName);
        if (userGroupsWorkSpace == null) {
            userGroupsWorkSpace = new UserGroups( "wrk_" + workSpaceName);
            userGroupsWorkSpace.setKieWorkbench(kieWorkbench);
            userGroupsWorkSpace.setSpaceName(workSpaceName);
            userGroupsRepository.save(userGroupsWorkSpace);
            User groupUser = new User( "wrk_user_" + workSpaceName, "pymma#");
            groupUser.getUserGroups().add(userGroupsWorkSpace);
            groupUser.getUserRoles().add(userRolesRepository.findByName("analyst"));
            groupUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
            userRepository.save(groupUser);
        }


        return userGroupsWorkSpace;
    }

    public Map<String, ProjectPersist> findProjectsConnectedUser() {
        //VaadinSession.getCurrent().get
        boolean isAdmin = false;

        UserConnected userConnected = userConnectedService.getUserConnected();
        User user = userRepository.findByLogin(userConnected.getUserName());
        for (UserRoles userRoles : user.getUserRoles()) {
            if ("admin".equals(userRoles.getName())) {
                isAdmin = true;
            }
        }
        List<ProjectPersist> projectPersists = new ArrayList<>();
        if (isAdmin) {
            projectPersists = projectRepository.findAll();
        } else {
            List<UserGroups> userGroups = user.getUserGroups();

        }
        Map<String, ProjectPersist> map = new HashMap<>();
        for (ProjectPersist projectPersist : projectPersists) {
            map.put(projectPersist.getProjectName().toString() + "-" + projectPersist.getBranch(), projectPersist);
        }

        return map;
    }

    public void removeAssociation(ProjectPersist projectPersist, List<RuntimePersist> runtimesRemove) {
        for (RuntimePersist runtimePersist : runtimesRemove) {
            List<ContainerRuntimePojoPersist> elts = containerRuntimeRepository.findByServerNameAndContainerId(runtimePersist.getServerName(), projectPersist.getContainerID());
            for (ContainerRuntimePojoPersist elt : elts) {
                elt.setStatus(ContainerRuntimePojoPersist.STATUS.TODELETE.name());
                containerRuntimeRepository.save(elt);
            }
        }
    }


    public boolean associate(ProjectPersist projectPersist, List<RuntimePersist> runtimePersists) {
        projectPersist.setStatus(ProjectPersist.Deployable);
        projectPersist.setContainerID(projectPersist.getDeploymentName() + "-" + projectPersist.getProjectName());
        projectPersist.getServerNames().clear();
        ReverseProxyUpdate reverseProxyUpdate = new ReverseProxyUpdate();
        reverseProxyUpdate.setContainerID(projectPersist.getContainerID());
        if (projectPersist.isUseJWTToConnect()) {
            reverseProxyUpdate.setTokenUUID(projectPersist.getUuid());
        }else{
            reverseProxyUpdate.setPath("/" + projectPersist.getContainerID());
        }
        for (RuntimePersist runtimePersist : runtimePersists) {
            List<String> names = new ArrayList<>();
            names.add(runtimePersist.getServerName());
            projectPersist.getServerNames().add(runtimePersist.getServerName());
            ContainerPojoPersist existingContainer = containerRepository.findByServerNameAndContainerId(runtimePersist.getServerName(), projectPersist.getContainerID());
            if (existingContainer == null) {
                ContainerPojoPersist newContainer = new ContainerPojoPersist();
                newContainer.setClassName(projectPersist.getMainClass());
                newContainer.setProcessID(projectPersist.getProcessID());
                newContainer.setContainerId(projectPersist.getContainerID());
                newContainer.setServerName(runtimePersist.getServerName());
                newContainer.setGroupId(projectPersist.getGroupID());
                newContainer.setArtifactId(projectPersist.getArtifactID());
                newContainer.setProjectUUID(projectPersist.getUuid());
                newContainer.setDisableRuleLogging(projectPersist.isDisableRuleLogging());
                newContainer.setVersion(projectPersist.getProjectVersion());
                containerRepository.save(newContainer);
                List<ContainerRuntimePojoPersist> elts = containerRuntimeRepository.findByServerNameAndContainerId(runtimePersist.getServerName(), projectPersist.getContainerID());
                if (!elts.isEmpty()) {
                    for (ContainerRuntimePojoPersist elt : elts) {
                        elt.setStatus(ContainerRuntimePojoPersist.STATUS.TODEPLOY.name());
                        containerRuntimeRepository.save(elt);
                    }
                } else {

                    ContainerRuntimePojoPersist runtimePojoPersist = new ContainerRuntimePojoPersist();
                    runtimePojoPersist.setServerName(runtimePersist.getServerName());
                    runtimePojoPersist.setHostname(runtimePersist.getHostname());
                    runtimePojoPersist.setContainerId(projectPersist.getContainerID());
                    runtimePojoPersist.setStatus(ContainerRuntimePojoPersist.STATUS.TODEPLOY.name());
                    runtimePojoPersist.setProjectUUID(projectPersist.getUuid());
                    runtimePojoPersist.setDisableRuleLogging(projectPersist.isDisableRuleLogging());
                    containerRuntimeRepository.save(runtimePojoPersist);
                }

            }else{
                existingContainer.setDisableRuleLogging(projectPersist.isDisableRuleLogging());
                existingContainer.setProjectUUID(projectPersist.getUuid());
                containerRepository.save(existingContainer);
            }

            String hostName = runtimePersist.getServerUrl() + "/api/" + projectPersist.getContainerID();
            reverseProxyUpdate.getServerNames().add(hostName);
        }
        projectRepository.save(projectPersist);
        jmsTemplate.send(KafkaTopicConstants.REVERSE_PROXY, new ReverseProxyMessageCreator(reverseProxyUpdate));

        return true;
    }

    public boolean deployer(ProjectPersist projectPersist, AddLog addLog, UI ui) {


        waitForJobToBeEnded(config.getKiewbUrl(), userConnectedService.getUserConnected().getUserName(),
                userConnectedService.getUserConnected().getUserPassword(), projectPersist, addLog, ui);

        return false;
    }

    public ProjectPersist platformProjectResponseToProjectPersist(PlatformProjectData platformProjectResponse) {
        ProjectPersist projectPersist = new ProjectPersist();
        projectPersist.setArtifactID(platformProjectResponse.getArtifactId());
        projectPersist.setGroupID(platformProjectResponse.getGroupId());
        projectPersist.setProjectName(new KieProject(platformProjectResponse.getSpaceName(), platformProjectResponse.getName()));
        projectPersist.setProjectVersion(platformProjectResponse.getVersion());
        projectPersist.setBranch(platformProjectResponse.getBranch());
        projectPersist.setStatus(ProjectPersist.ADEFINIR);
        projectPersist.setClassNameList(platformProjectResponse.getJavaClasses());
        return projectPersist;
    }


    public void waitForJobToBeEnded(String url, String username, String password, ProjectPersist projectPersist, AddLog workOnGoingView, UI ui) {

        UserConnected userConnected = userConnectedService.getUserConnected();

        Thread thread = new Thread() {
            @Override
            public void run() {

                JobStatus result = kieRepositoryService.buildProject(config.getKiewbUrl(), userConnected.getUserName(),
                        userConnected.getUserPassword(), projectPersist.getProjectName().getSpaceName(), projectPersist.getProjectName().getName(), projectPersist.getBranch(), "compile", workOnGoingView, ui);

                jobService.executeWrite(url, username, password, workOnGoingView, result.getJobId(), ui);

                result = kieRepositoryService.buildProject(config.getKiewbUrl(), userConnected.getUserName(),
                        userConnected.getUserPassword(), projectPersist.getProjectName().getSpaceName(), projectPersist.getProjectName().getName(), projectPersist.getBranch(), "install", workOnGoingView, ui);

                jobService.executeWrite(url, username, password, workOnGoingView, result.getJobId(), ui);


                for (String serverName : projectPersist.getServerNames()) {


                    List<ContainerRuntimePojoPersist> existingContainers = containerRuntimeRepository.findByServerNameAndContainerId(serverName, projectPersist.getContainerID());
                    if (!existingContainers.isEmpty()) {
                        for (ContainerRuntimePojoPersist containerRuntimePojoPersist : existingContainers) {
                            containerRuntimePojoPersist.setStatus(ContainerRuntimePojoPersist.STATUS.TODEPLOY.name());
                            containerRuntimeRepository.save(containerRuntimePojoPersist);
                        }
                    } else {
                        List<RuntimePersist> servers = runtimeRepository.findByServerName(serverName);
                        for (RuntimePersist server : servers) {
                            ContainerRuntimePojoPersist runtimePojoPersist = new ContainerRuntimePojoPersist();
                            runtimePojoPersist.setServerName(serverName);
                            runtimePojoPersist.setHostname(server.getHostname());
                            runtimePojoPersist.setContainerId(projectPersist.getContainerID());
                            runtimePojoPersist.setStatus(ContainerRuntimePojoPersist.STATUS.TODEPLOY.name());
                            runtimePojoPersist.setProjectUUID(projectPersist.getUuid());
                            containerRuntimeRepository.save(runtimePojoPersist);
                        }
                    }
                }
            }
        };
        thread.start();

    }


    public ProjectRepository getProjectRepository() {
        return projectRepository;
    }

    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
}
