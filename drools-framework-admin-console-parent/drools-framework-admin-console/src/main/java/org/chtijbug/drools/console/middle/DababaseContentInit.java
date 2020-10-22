package org.chtijbug.drools.console.middle;

import org.chtijbug.drools.console.service.KieRepositoryService;
import org.chtijbug.drools.console.service.ProjectPersistService;
import org.chtijbug.drools.console.service.model.UserConnected;
import org.chtijbug.drools.proxy.persistence.model.*;
import org.chtijbug.drools.proxy.persistence.repository.*;
import org.chtijbug.guvnor.server.jaxrs.model.PlatformProjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DababaseContentInit {

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
        }else{
            this.synchronizeDatabaseWithWorkbenches();
        }

    }

    private void initDatabase(){
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
        adminUser.getUserGroups().add(userGroupsRepository.findByName("kiemgmt"));
        adminUser.getUserGroups().add(userGroupsRepository.findByName("admingroup"));
        adminUser.getUserRoles().add(userRolesRepository.findByName("admin"));
        adminUser.getUserRoles().add(userRolesRepository.findByName("analyst"));
        adminUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        userRepository.save(adminUser);

        User nheronUser = new User(UUID.randomUUID().toString(), "nheron", "adminnheron00@");
        nheronUser.getUserGroups().add(userGroupsRepository.findByName("kiemgmt"));
        nheronUser.getUserGroups().add(userGroupsRepository.findByName("admingroup"));
        nheronUser.getUserRoles().add(userRolesRepository.findByName("admin"));
        nheronUser.getUserRoles().add(userRolesRepository.findByName("analyst"));
        nheronUser.getUserRoles().add(userRolesRepository.findByName("rest-all"));
        userRepository.save(nheronUser);

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
            ProjectPersist projectPersist=projectPersistService.saveorUpdateProject(platformProjectData, mainWorkbench);
            UserGroups workspaceUserGroups=projectPersistService.createWorkSpaceGroupIfNeeded(workspaceName, mainWorkbench);
            projectPersistService.createProjectGroupIfNeeded(projectName, mainWorkbench,projectPersist,workspaceUserGroups);

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

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void synchronizeDatabaseWithWorkbenches(){
        for (KieWorkbench kieWorkbench : kieWorkbenchRepository.findAll()){
            User nheronUser = userRepository.findByLogin("nheron");
            UserConnected userConnected = kieRepositoryService.getUserContent(kieWorkbench.getExternalUrl() + "/rest",
                    nheronUser.getLogin(), nheronUser.getPassword(), kieWorkbench.getName());
            for (PlatformProjectData platformProjectData : userConnected.getProjectResponses()) {

                String projectName = platformProjectData.getName();
                String workspaceName = platformProjectData.getSpaceName();
                ProjectPersist projectPersist=projectPersistService.saveorUpdateProject(platformProjectData, kieWorkbench);
                UserGroups workSpaceGroupIfNeeded = projectPersistService.createWorkSpaceGroupIfNeeded(workspaceName, kieWorkbench);
                projectPersistService.createProjectGroupIfNeeded(projectName, kieWorkbench,projectPersist,workSpaceGroupIfNeeded);




            }
        }

    }

}
